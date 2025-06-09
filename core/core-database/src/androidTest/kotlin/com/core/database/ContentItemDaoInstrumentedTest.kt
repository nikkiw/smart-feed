package com.core.database


import android.content.Context
import androidx.paging.PagingSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.content.ArticleAttributesEntity
import com.core.database.content.ContentDao
import com.core.database.content.ContentDatabase
import com.core.database.content.ContentUpdateEntity
import com.core.database.content.contentItemPagingSource
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import com.core.utils.DateTimeConvertors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class ContentItemDaoInstrumentedTest {

    private lateinit var db: ContentDatabase
    private lateinit var contentDao: ContentDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = ContentDatabase.getTestDatabase(context)
        contentDao = db.contentDao()
    }

    @After
    fun closeDb() {
        db.close()
    }


    @Test
    fun testPagingSource_queryByTypeAndTag_andSortedByNameAsc() = runTest {
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

        val tags = db.contentTagsDao().allTags().first()
        val expectedTags = listOf("news", "tech", "science")
        assertEquals(expectedTags.toSet(), tags.toSet())

        // Query by type ARTICLE and tag "news", sort by name ASC
        val query = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = Tags(listOf("news")),
            sortedBy = ContentItemsSortedType.ByNameAsc
        )

        val pagingSource = contentItemPagingSource(query, contentDao)
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        // We expect 2 results in sorted order by title ("Alpha Title", "Beta Title")
        val expectedOrder = listOf("id1", "id2")
        val actualOrder = when (loadResult) {
            is PagingSource.LoadResult.Page -> loadResult.data.map { it.contentUpdate.id }
            else -> emptyList()
        }
        assertEquals(expectedOrder, actualOrder)
    }

    @Test
    fun testPagingSource_queryByTagOnly_andSortedByDateDesc() = runTest {
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


        // Query by tag "tech", sort by date descending (newest first)
        val query = Query(
            types = emptyList(),
            tags = Tags(listOf("tech")),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )

        val pagingSource = contentItemPagingSource(query, contentDao)
        val loadResult = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 10,
                placeholdersEnabled = false
            )
        )

        // Only entity2 has "tech" tag, it should be the only result
        val expectedOrder = listOf("id2")
        val actualOrder = when (loadResult) {
            is PagingSource.LoadResult.Page -> loadResult.data.map { it.contentUpdate.id }
            else -> emptyList()
        }
        assertEquals(expectedOrder, actualOrder)
    }
}
