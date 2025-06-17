package com.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.entity.ContentEntity
import com.core.database.embeding.ArticleEmbedding
import com.core.domain.model.ContentType
import com.core.utils.DateTimeConvertors
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.assertEquals


@RunWith(AndroidJUnit4::class)
class ArticleEmbeddingDaoTest {

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
    fun testAllEmbeddings() = runTest {
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

        val embeddings = db.articleEmbeddingDao().allEmbeddings().sortedBy { it.articleId }
        val expectedEmbeddings = listOf(
            ArticleEmbedding(entity1.id, attr1.unitEmbedding),
            ArticleEmbedding(entity2.id, attr2.unitEmbedding),
        ).sortedBy { it.articleId }
        assertEquals(expectedEmbeddings, embeddings)

    }
}