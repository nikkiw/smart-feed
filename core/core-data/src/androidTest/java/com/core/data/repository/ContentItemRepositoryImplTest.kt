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
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ContentItemRepositoryImplTest {

    private lateinit var db: ContentDatabase
    private lateinit var contentDao: ContentDao
    private lateinit var repo: ContentItemRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = ContentDatabase.Companion.getTestDatabase(context)
        contentDao = db.contentDao()
        repo = ContentItemRepositoryImpl(
            contentDao = contentDao,
            updatesMetaDao = db.updatesMetaDao(),
            networkDataSource = DevNetworkDataSource(),
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
            content = "Alpha Content"
        )
        val attr2 = ArticleAttributesEntity(
            contentUpdateId = "id2",
            title = "Beta Title",
            content = "Beta Content"
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

        // Query by type ARTICLE and tag "тег1", sort by name ASC
        val query = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = Tags(listOf("тег1")),
            sortedBy = ContentItemsSortedType.ByNameAsc
        )


        val expectedOrder = setOf(
            "article-100",
            "article-101",
            "article-103",
            "article-104",
            "article-106",
            "article-107",
            "article-109",
            "article-110",
            "article-112",
            "article-113",
            "article-115",
            "article-116",
            "article-118",
            "article-119",
            "article-121",
            "article-122",
            "article-124",
            "article-125",
            "article-127",
            "article-128",
            "article-130",
            "article-131",
            "article-133",
            "article-134",
            "article-136",
            "article-137",
            "article-139",
            "article-140",
            "article-142",
            "article-143",
            "article-145",
            "article-146",
            "article-148",
            "article-149",
            "article-52",
            "article-53",
            "article-55",
            "article-56",
            "article-58",
            "article-59",
            "article-61",
            "article-62",
            "article-64",
            "article-65",
            "article-67",
            "article-68",
            "article-70",
            "article-71",
            "article-73",
            "article-74",
            "article-76",
            "article-77",
            "article-79",
            "article-80",
            "article-82",
            "article-83",
            "article-85",
            "article-86",
            "article-88",
            "article-89"
        )

        val actualOrder = repo.flowContent(query)
            .asSnapshot()
            .map { it.id.value }
        assertEquals(expectedOrder, actualOrder.toSet())
    }
}