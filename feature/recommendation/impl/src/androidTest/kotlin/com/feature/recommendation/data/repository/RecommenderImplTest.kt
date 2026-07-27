package com.feature.recommendation.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.analytics.local.entity.EventLog
import com.core.analytics.local.entity.EventType
import com.core.content.embedding.EmbeddingIndex
import com.core.content.model.ContentId
import com.core.content.model.ContentLanguage
import com.core.content.model.Embeddings
import com.core.database.AppDatabase
import com.core.networks.datasource.dev.DevStaticJsonTestNetworkDataSource
import com.core.networks.models.ContentAttributes
import com.feature.feed.local.content.entity.ArticleAttributesEntity
import com.feature.feed.local.content.entity.ContentEntity
import com.feature.recommendation.data.service.RecommenderImpl
import com.feature.recommendation.domain.repository.RecommendationRepository
import com.feature.recommendation.domain.service.Recommender
import com.feature.userprofile.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class RecommenderImplTest {
    private lateinit var networkDataSource: DevStaticJsonTestNetworkDataSource
    private lateinit var db: AppDatabase
    private lateinit var userProfileRepository: FakeUserProfileRepository
    private lateinit var recommender: Recommender
    private lateinit var recommendationRepository: RecommendationRepository
    private lateinit var applicationScope: CoroutineScope

    private val topK: Int = 10
    private val coldK: Int = 4
    private val mmrK: Int = 5
    private val lambda: Float = 0.7f

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testDispatcher = UnconfinedTestDispatcher()

        applicationScope = CoroutineScope(SupervisorJob() + testDispatcher)

        networkDataSource = DevStaticJsonTestNetworkDataSource(context)
        db = AppDatabase.getTestDatabase(context)

        userProfileRepository = FakeUserProfileRepository(db)
        recommender =
            RecommenderImpl(
                userProfileRepository = userProfileRepository,
                contentInteractionStatsDao = db.articleInteractionStatsDao(),
                contentDao = db.contentDao(),
                articleEmbeddingDao = db.articleEmbeddingDao(),
                recommendationDao = db.recommendationDao(),
                ioDispatcher = testDispatcher,
                defaultDispatcher = testDispatcher,
                applicationScope = applicationScope,
                topK = topK,
                coldK = coldK,
                mmrK = mmrK,
                lambda = lambda,
            )
        recommendationRepository =
            RecommendationRepositoryImpl(
                recommendationDao = db.recommendationDao(),
                ioDispatcher = testDispatcher,
            )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRecommendForUser_for_empty_profile() = runTest {
        // когда нет никакого профиля, возвращается 5 последний загруженных статей
        seedContent()

        val expectedRecommendations =
            db.contentDao().getRecentContent(mmrK).map { it.contentUpdate.id }

        recommender.updateRecommendationsForUser()

        val actualRecommendations =
            recommendationRepository.recommendForUser().first().map { it.articleId.value }

        assertEquals(expectedRecommendations, actualRecommendations)
    }

    @Test
    fun testRecommendForUser_recommendation_after_on_article_read() = runTest {
        // когда нет никакого профиля, возвращается 5 последний загруженных статей
        seedContent()

        val articleRead =
            db.contentDao().getRecentContent(1).first()

        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleRead.contentUpdate.id,
                eventType = EventType.READ,
                readPercentage = 0.3f,
                readingTimeMillis = 2000,
            ),
        )

        userProfileRepository.onArticleVisited(ContentId(articleRead.contentUpdate.id))

        val expectedRecommendations =
            db.contentDao().getRecentContent(mmrK).map { it.contentUpdate.id }

        recommender.updateRecommendationsForUser()

        val actualRecommendations =
            recommendationRepository.recommendForUser().first().map { it.articleId.value }

        assertNotEquals(expectedRecommendations, actualRecommendations)
    }

    @Test
    fun testRecommendForUser_prefers_dominant_read_language() = runTest {
        seedArticle(
            id = "en-old",
            title = "English article one",
            shortDescription = "English short",
            content = "English body one",
            updatedAt = "2025-01-01T10:00:00Z",
            languageCode = ContentLanguage.ENGLISH.code,
            embedding = floatArrayOf(1f, 0f, 0f),
        )
        seedArticle(
            id = "en-new",
            title = "English article two",
            shortDescription = "English short",
            content = "English body two",
            updatedAt = "2025-01-03T10:00:00Z",
            languageCode = ContentLanguage.ENGLISH.code,
            embedding = floatArrayOf(0.9f, 0.1f, 0f),
        )
        seedArticle(
            id = "ru-newest",
            title = "Русская статья",
            shortDescription = "Короткое описание",
            content = "Русский текст статьи",
            updatedAt = "2025-01-04T10:00:00Z",
            languageCode = ContentLanguage.RUSSIAN.code,
            embedding = floatArrayOf(0f, 1f, 0f),
        )

        db.eventLogDao().insertEvent(
            EventLog(
                contentId = "en-new",
                eventType = EventType.READ,
                readPercentage = 1f,
                readingTimeMillis = 12_000,
            ),
        )

        recommender.updateRecommendationsForUser()

        val actualRecommendations =
            recommendationRepository.recommendForUser().first().map { it.articleId.value }

        assertEquals(listOf("en-new", "en-old"), actualRecommendations)
    }

    @Test
    fun testRecommendForArticle_stays_within_article_language_when_possible() = runTest {
        seedArticle(
            id = "en-source",
            title = "English source article",
            shortDescription = "English short",
            content = "English body source",
            updatedAt = "2025-02-01T10:00:00Z",
            languageCode = ContentLanguage.ENGLISH.code,
            embedding = floatArrayOf(1f, 0f, 0f),
        )
        seedArticle(
            id = "en-related",
            title = "English related article",
            shortDescription = "English short",
            content = "English body related",
            updatedAt = "2025-02-02T10:00:00Z",
            languageCode = ContentLanguage.ENGLISH.code,
            embedding = floatArrayOf(0.95f, 0.05f, 0f),
        )
        seedArticle(
            id = "ru-related",
            title = "Русская похожая статья",
            shortDescription = "Короткое описание",
            content = "Русский текст похожей статьи",
            updatedAt = "2025-02-03T10:00:00Z",
            languageCode = ContentLanguage.RUSSIAN.code,
            embedding = floatArrayOf(0.96f, 0.04f, 0f),
        )

        recommender.updateRecommendationsForArticles()

        val recommendations =
            recommendationRepository.recommendForArticle(ContentId("en-source"))
                .first()
                .map { it.articleId.value }

        assertTrue(recommendations.isNotEmpty())
        assertTrue("en-related" in recommendations)
        assertTrue("ru-related" !in recommendations)
    }

    private suspend fun seedContent() {
        val updates = networkDataSource.getUpdates(since = "1970-01-01T00:00:00Z").getOrThrow().data
        Assert.assertTrue(updates.isNotEmpty())

        updates.forEach { update ->
            val entity =
                ContentEntity(
                    id = update.id,
                    type = update.type,
                    action = update.action,
                    updatedAt = Instant.parse(update.updatedAt).toEpochMilli(),
                    mainImageUrl = update.mainImageUrl,
                    languageCode =
                    (update.attributes as? ContentAttributes.Article)?.let {
                        ContentLanguage.resolve(
                            explicitCode = it.languageCode,
                            title = it.title,
                            shortDescription = it.shortDescription,
                            content = it.content,
                        ).code
                    } ?: ContentLanguage.UNDETERMINED.code,
                    tags = update.tags,
                )
            val articleEntity =
                (update.attributes as? ContentAttributes.Article)?.let {
                    ArticleAttributesEntity(
                        contentId = update.id,
                        title = it.title,
                        shortDescription = it.shortDescription,
                        content = it.content,
                        unitEmbedding =
                        EmbeddingIndex.normalize(
                            it.embeddings.data.map { embedding -> embedding.toFloat() }
                                .toFloatArray(),
                        ),
                    )
                }
            db.contentDao().insertContentUpdateWithDetails(entity, articleEntity)
        }
    }

    private suspend fun seedArticle(
        id: String,
        title: String,
        shortDescription: String,
        content: String,
        updatedAt: String,
        languageCode: String,
        embedding: FloatArray,
    ) {
        db.contentDao().insertContentUpdateWithDetails(
            ContentEntity(
                id = id,
                type = "article",
                action = "upsert",
                updatedAt = Instant.parse(updatedAt).toEpochMilli(),
                mainImageUrl = "https://example.com/$id.png",
                languageCode = languageCode,
                tags = listOf(languageCode),
            ),
            ArticleAttributesEntity(
                contentId = id,
                title = title,
                shortDescription = shortDescription,
                content = content,
                unitEmbedding = EmbeddingIndex.normalize(embedding),
            ),
        )
    }

    private class FakeUserProfileRepository(
        private val db: AppDatabase,
    ) : UserProfileRepository {
        private val profile = MutableStateFlow<Embeddings?>(null)

        override suspend fun onArticleVisited(articleId: ContentId): Embeddings? {
            val embeddings =
                db.articleEmbeddingDao()
                    .getEmbeddings(articleId.value)
                    ?.unitEmbedding
                    ?.let(::Embeddings)
            profile.value = embeddings
            return embeddings
        }

        override suspend fun getUserProfileEmbeddings(): Flow<Embeddings?> = profile
    }
}
