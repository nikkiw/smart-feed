package com.core.database


import android.content.Context
import androidx.paging.PagingSource
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.content.contentItemPagingSource
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.entity.ContentEntity
import com.core.domain.model.ContentType
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
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ContentDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getTestDatabase(context)
    }

    @After
    fun closeDb() {
        db.close()
    }


    @Test
    fun testPagingSource_queryByTypeAndTag_andSortedByNameAsc() = runTest {
        // Prepare data
        val entity1 = ContentEntity(
            id = "id1",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )
        val entity2 = ContentEntity(
            id = "id2",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-02T10:00:00"),
            mainImageUrl = "url2",
            tags = listOf("news", "tech")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = "id1",
            title = "Alpha Title",
            content = "Alpha Content",
            shortDescription = "Short desc",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        val attr2 = ArticleAttributesEntity(
            contentId = "id2",
            title = "Beta Title",
            content = "Beta Content",
            shortDescription = "Short desc",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)
        db.contentDao().insertContentUpdateWithDetails(entity2, attr2)

        val tags = db.contentTagsDao().allTags().first()
        val expectedTags = listOf("news", "tech", "science")
        assertEquals(expectedTags.toSet(), tags.toSet())

        // Query by type ARTICLE and tag "news", sort by name ASC
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(listOf("news")),
            sortedBy = ContentItemsSortedType.ByNameAsc
        )

        val pagingSource = contentItemPagingSource(query, db.contentDao())
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
        val entity1 = ContentEntity(
            id = "id1",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )
        val entity2 = ContentEntity(
            id = "id2",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-02T10:00:00"),
            mainImageUrl = "url2",
            tags = listOf("news", "tech")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = "id1",
            title = "Alpha Title",
            content = "Alpha Content",
            shortDescription = "Short desc",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        val attr2 = ArticleAttributesEntity(
            contentId = "id2",
            title = "Beta Title",
            content = "Beta Content",
            shortDescription = "Short desc",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )

        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)
        db.contentDao().insertContentUpdateWithDetails(entity2, attr2)


        // Query by tag "tech", sort by date descending (newest first)
        val query = Query(
            types = emptyList(),
            tags = Tags(listOf("tech")),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )

        val pagingSource = contentItemPagingSource(query, db.contentDao())
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

    @Test
    fun testFlowContent_check_embeddings_convertors() = runTest {
        // Prepare data
        val entity1 = ContentEntity(
            id = "id1",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = "id1",
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)


        // Query by type ARTICLE and tag "news", sort by name ASC
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(),
            sortedBy = ContentItemsSortedType.ByNameAsc
        )


        // We expect 2 results in sorted order by title ("Alpha Title", "Beta Title")
        val expectedEmbeddings = attr1.unitEmbedding.toList()
        val actualEmbeddings =
            db.contentDao().getContentById(entity1.id).article?.unitEmbedding?.toList()
        assertEquals(expectedEmbeddings, actualEmbeddings)
    }


    @Test
    fun test_is_not_empty() = runTest {
        // Prepare data
        val entity1 = ContentEntity(
            id = "id1",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = "id1",
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)

        val isNotEmpty = db.contentDao().isNotEmpty()

        assertTrue(isNotEmpty)
    }

    @Test
    fun test_get_recent_content() = runTest {
        // Prepare data
        val entity1 = ContentEntity(
            id = "id1",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = entity1.id,
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)

        val entity2 = ContentEntity(
            id = "id2",
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2025-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr2 = ArticleAttributesEntity(
            contentId = entity2.id,
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = FloatArray(10) { Random.nextDouble(-1.0, 1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(entity2, attr2)


        val actualIds = db.contentDao().getRecentContent(1).map { it.contentUpdate.id }
        val exceptedIds = listOf(entity2.id)
        assertEquals(exceptedIds, actualIds)
    }

}
