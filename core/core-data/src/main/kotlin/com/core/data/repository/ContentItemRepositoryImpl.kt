package com.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.core.data.dto.toContentItem
import com.core.database.content.ArticleAttributesEntity
import com.core.database.content.ContentDao
import com.core.database.content.ContentTagsDao
import com.core.database.content.ContentUpdateEntity
import com.core.database.content.UpdatesMetaDao
import com.core.database.content.UpdatesMetaEntity
import com.core.database.content.contentItemPagingSource
import com.core.di.IoDispatcher
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.Query
import com.core.networks.datasource.NetworkDataSource
import com.core.networks.models.ContentAttributes
import com.core.runSuspendCatching
import com.core.utils.DateTimeConvertors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class ContentItemRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val contentTagsDao: ContentTagsDao,
    private val updatesMetaDao: UpdatesMetaDao,
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentItemRepository {

    private val mutex = Mutex()

    override fun flowContent(query: Query): Flow<PagingData<ContentItem>> {
        // Pager config can be tuned as needed
        return Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = {
                contentItemPagingSource(
                    query = query,
                    contentDao = contentDao
                )
            }
        )
            .flow
            .mapNotNull { pagingData ->
                pagingData.map { it.toContentItem() }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getContentById(itemId: ContentItemId): Result<ContentItem> =
        withContext(ioDispatcher) {
            runCatching {
                contentDao.getContentById(itemId.value).toContentItem()
            }
        }

    override suspend fun isEmpty(): Boolean = withContext(ioDispatcher) {
        !contentDao.isNotEmpty()
    }

    override fun flowAllTags(): Flow<Tags> {
        return contentTagsDao.allTags()
            .map {
                Tags(it)
            }.flowOn(ioDispatcher)
    }


    override suspend fun syncContent(): Result<Unit> {
        return runSuspendCatching {
            withContext(ioDispatcher) {
                mutex.withLock {
                    val lastSyncAt = updatesMetaDao.getMeta()?.lastSyncAt ?: "1970-01-01T00:00:00Z"
                    val networkResult = networkDataSource.getUpdates(since = lastSyncAt)
                    if (networkResult.isSuccess) {
                        val updates = networkResult.getOrThrow().data
                        // Save updates in DB
                        updates.forEach { update ->
                            val type = update.type
                            val entity = ContentUpdateEntity(
                                id = update.id,
                                type = type,
                                action = update.action,
                                updatedAt = DateTimeConvertors.parseIsoToLongMs(update.updatedAt),
                                mainImageUrl = update.mainImageUrl,
                                tags = update.tags
                            )
                            val articleEntity = when (ContentItemType.fromString(type)) {
                                ContentItemType.ARTICLE -> (update.attributes as? ContentAttributes.Article)?.let {
                                    ArticleAttributesEntity(
                                        contentUpdateId = update.id,
                                        title = it.title,
                                        content = it.content
                                    )
                                }

                                else -> null
                            }
                            // You can add quiz mapping here if needed
                            contentDao.insertContentUpdateWithDetails(
                                contentUpdate = entity,
                                article = articleEntity
                            )
                        }
                        // Save new lastSyncAt
                        val maxUpdatedAt = updates.maxOfOrNull { it.updatedAt } ?: lastSyncAt
                        updatesMetaDao.saveMeta(UpdatesMetaEntity(lastSyncAt = maxUpdatedAt))
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            networkResult.exceptionOrNull() ?: Exception("Unknown error")
                        )
                    }
                }
            }
        }
    }
}
