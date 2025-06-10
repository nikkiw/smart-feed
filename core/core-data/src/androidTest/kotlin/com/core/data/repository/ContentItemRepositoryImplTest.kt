package com.core.data.repository

import android.content.Context
import androidx.paging.testing.asSnapshot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.content.ArticleAttributesEntity
import com.core.database.content.ContentDao
import com.core.database.content.ContentDatabase
import com.core.database.content.ContentUpdateEntity
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import com.core.networks.datasource.dev.DevNetworkDataSource
import com.core.utils.DateTimeConvertors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ContentItemRepositoryImplTest {

    private lateinit var networkDataSource: DevNetworkDataSource
    private lateinit var db: ContentDatabase
    private lateinit var contentDao: ContentDao
    private lateinit var repo: ContentItemRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        networkDataSource = DevNetworkDataSource()
        db = ContentDatabase.Companion.getTestDatabase(context)
        contentDao = db.contentDao()
        repo = ContentItemRepositoryImpl(
            contentDao = contentDao,
            contentTagsDao = db.contentTagsDao(),
            updatesMetaDao = db.updatesMetaDao(),
            networkDataSource = networkDataSource,
            ioDispatcher = UnconfinedTestDispatcher()
        )
    }

    @After
    fun closeDb() {
        db.close()
    }


    @Test
    fun testFlowContent_queryByTypeAndTag_andSortedByNameAsc() = runTest {
        // Prepare data
        val entity1 = ContentUpdateEntity(
            id = "id1",
            type = ContentItemType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )
        val entity2 = ContentUpdateEntity(
            id = "id2",
            type = ContentItemType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-02T10:00:00"),
            mainImageUrl = "url2",
            tags = listOf("news", "tech")
        )

        val attr1 = ArticleAttributesEntity(
            contentUpdateId = "id1",
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            embeddings = FloatArray(10){ Random.nextDouble(-1.0,1.0).toFloat() }
        )
        val attr2 = ArticleAttributesEntity(
            contentUpdateId = "id2",
            title = "Beta Title",
            shortDescription = "Short desc",
            content = "Beta Content",
            embeddings = FloatArray(10){ Random.nextDouble(-1.0,1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)
        db.contentDao().insertContentUpdateWithDetails(entity2, attr2)


        // Query by type ARTICLE and tag "news", sort by name ASC
        val query = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = Tags(listOf("news")),
            sortedBy = ContentItemsSortedType.ByNameAsc
        )


        // We expect 2 results in sorted order by title ("Alpha Title", "Beta Title")
        val expectedOrder = listOf("id1", "id2")
        val actualOrder = repo.flowContent(query)
            .asSnapshot()
            .map { it.id.value }
        assertEquals(expectedOrder, actualOrder)
    }


    @Test
    fun testSyncContentAll_queryByTagOnly_andSortedByDateDesc() = runTest {

        // Act
        val result = repo.syncContent()

        // Assert: проверьте, что результат успешен
        Assert.assertTrue(result.isSuccess)

        val tags = networkDataSource.allTags.subList(0, 1)

        // Query by type ARTICLE and tag "тег1", sort by name ASC
        val query = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = Tags(tags),
            sortedBy = ContentItemsSortedType.ByNameAsc
        )


        val expectedOrder = networkDataSource.dummyData
            .filter { content ->
                content.type == ContentItemType.ARTICLE.toString()
                        && content.action != "delete"
                        && content.tags.any { it in tags }
            }.map { it.id }
            .sorted()
            .toSet()

        val actualOrder = repo.flowContent(query)
            .asSnapshot {
                scrollTo(index = Int.MAX_VALUE)
            }
            .map { it.id.value }
            .sorted()
            .toSet()

        assertEquals(expectedOrder, actualOrder.toSet())
    }
}