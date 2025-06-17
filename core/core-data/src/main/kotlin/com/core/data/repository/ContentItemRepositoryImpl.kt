package com.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.core.data.dto.toContentItem
import com.core.data.dto.toContentPreview
import com.core.data.embedding.EmbeddingIndex
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.ContentDao
import com.core.database.content.ContentTagsDao
import com.core.database.content.entity.ContentEntity
import com.core.database.content.UpdatesMetaDao
import com.core.database.content.entity.UpdatesMetaEntity
import com.core.database.content.contentItemPagingSource
import com.core.di.IoDispatcher
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview
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


class ContentItemRepositoryImpl @Inject constructor(
    private val contentDao: ContentDao,
    private val contentTagsDao: ContentTagsDao,
    private val updatesMetaDao: UpdatesMetaDao,
    private val networkDataSource: NetworkDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ContentItemRepository {

    private val mutex = Mutex()

    override fun flowContent(query: Query): Flow<PagingData<ContentItemPreview>> {
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
                pagingData.map { it.toContentPreview() }
            }
            .flowOn(ioDispatcher)
    }

    override suspend fun getContentById(itemId: ContentId): Result<ContentItem> =
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
                    var since = updatesMetaDao.getMeta()?.lastSyncAt ?: "1970-01-01T00:00:00Z"
                    do {
                        // Запрос очередной страницы
                        val networkResult = networkDataSource.getUpdates(since = since)
                        if (networkResult.isFailure) {
                            val error =
                                networkResult.exceptionOrNull() ?: Exception("Unknown error")
                            throw error
                        }
                        val response = networkResult.getOrThrow()
                        val updates = response.data

                        // Сохраняем данные в БД
                        updates.forEach { update ->
                            val entity = ContentEntity(
                                id = update.id,
                                type = update.type,
                                action = update.action,
                                updatedAt = DateTimeConvertors.parseIsoToLongMs(update.updatedAt),
                                mainImageUrl = update.mainImageUrl,
                                tags = update.tags
                            )

                            val articleEntity =
                                (update.attributes as? ContentAttributes.Article)?.let {
                                    ArticleAttributesEntity(
                                        contentId = update.id,
                                        title = it.title,
                                        shortDescription = it.shortDescription,
                                        content = it.content,
                                        unitEmbedding = EmbeddingIndex.normalize(
                                            it.embeddings.data.map { it.toFloat() }
                                            .toFloatArray()
                                        )
                                    )
                                }
                            contentDao.insertContentUpdateWithDetails(
                                contentUpdate = entity,
                                article = articleEntity
                            )
                        }

                        // Обновляем since для следующей итерации
                        since = response.meta.nextSince
                        // Цикл повторится, пока сервер говорит, что есть ещё
                    } while (response.meta.hasMore)

                    // После загрузки всех страниц сохраняем новую точку синка
                    updatesMetaDao.saveMeta(UpdatesMetaEntity(lastSyncAt = since))
                    Result.success(Unit)
                }
            }
        }
    }
}
