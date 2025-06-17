package com.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.data.service.RecommenderImpl
import com.core.database.AppDatabase
import com.core.database.event.entity.EventLog
import com.core.database.event.entity.EventType
import com.core.domain.model.ContentId
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.RecommendationRepository
import com.core.domain.repository.UserProfileRepository
import com.core.domain.service.Recommender
import com.core.networks.datasource.dev.DevStaticJsonTestNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals


@RunWith(AndroidJUnit4::class)
class RecommenderImplTest {

    private lateinit var networkDataSource: DevStaticJsonTestNetworkDataSource
    private lateinit var db: AppDatabase
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var contentRepo: ContentItemRepository
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
        db = AppDatabase.Companion.getTestDatabase(context)

        userProfileRepository = UserProfileRepositoryImpl(
            embeddingDao = db.articleEmbeddingDao(),
            contentInteractionStatsDao = db.articleInteractionStatsDao(),
            userProfileDao = db.userProfileDao(),
            ioDispatcher = testDispatcher
        )
        contentRepo = ContentItemRepositoryImpl(
            contentDao = db.contentDao(),
            contentTagsDao = db.contentTagsDao(),
            updatesMetaDao = db.updatesMetaDao(),
            networkDataSource = networkDataSource,
            ioDispatcher = testDispatcher
        )
        recommender = RecommenderImpl(
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
            lambda = lambda
        )
        recommendationRepository = RecommendationRepositoryImpl(
            recommendationDao = db.recommendationDao(),
            ioDispatcher = testDispatcher
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testRecommendForUser_for_empty_profile() = runTest {
        // когда нет никакого профиля, возвращается 5 последний загруженных статей
        // Act
        val result = contentRepo.syncContent()

        // Assert: проверьте, что результат успешен
        Assert.assertTrue(result.isSuccess)

        var expectedRecommendations =
            db.contentDao().getRecentContent(mmrK).map { it.contentUpdate.id }


        recommender.updateRecommendationsForUser()

        val actualRecommendations =
            recommendationRepository.recommendForUser().first().map { it.articleId.value }

        assertEquals(expectedRecommendations, actualRecommendations)
    }

    @Test
    fun testRecommendForUser_recommendation_after_on_article_read() = runTest {
        // когда нет никакого профиля, возвращается 5 последний загруженных статей
        // Act
        val result = contentRepo.syncContent()

        // Assert: проверьте, что результат успешен
        Assert.assertTrue(result.isSuccess)

        var articleRead =
            db.contentDao().getRecentContent(1).first()

        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleRead.contentUpdate.id,
                eventType = EventType.READ,
                readPercentage = 0.3f,
                readingTimeMillis = 2000,
            )
        )

        userProfileRepository.onArticleVisited(ContentId(articleRead.contentUpdate.id))


        var expectedRecommendations =
            db.contentDao().getRecentContent(mmrK).map { it.contentUpdate.id }

        recommender.updateRecommendationsForUser()

        val actualRecommendations =
            recommendationRepository.recommendForUser().first().map { it.articleId.value }

        assertNotEquals(expectedRecommendations, actualRecommendations)
    }
}