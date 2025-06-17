package com.core.data.repository


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.core.data.di.CoroutinesTestModule
import com.core.database.AppDatabase
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.entity.ContentEntity
import com.core.database.event.entity.EventLog
import com.core.database.event.entity.EventType
import com.core.domain.model.ContentId
import com.core.domain.model.ContentType
import com.core.domain.repository.UserProfileRepository
import com.core.utils.DateTimeConvertors
import com.google.common.truth.Truth.assertWithMessage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@HiltAndroidTest
class UserProfileRepositoryImplTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context


    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = CoroutinesTestModule.testDispatcher

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var repository: UserProfileRepository

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        hiltRule.inject()
        Dispatchers.setMain(testDispatcher)

        context = ApplicationProvider.getApplicationContext()

//        val context = ApplicationProvider.getApplicationContext<Context>()
//        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
//
//        repository = UserProfileRepositoryImpl(
//            embeddingDao = db.articleEmbeddingDao(),
//            contentInteractionStatsDao = db.contentInteractionStatsDao(),
//            userProfileDao = db.userProfileDao(),
//            ioDispatcher = testDispatcher
//        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.clearAllTables()
    }

    @Test
    fun test_empty_profile() = runTest {
        val profile = repository.getUserProfileEmbeddings().first()
        assertNull(profile)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_firstVisit_createsProfile() = runTest {
        // Arrange
        val articleId = "article_1"
        val dummyEmbedding = floatArrayOf(0.1f, 0.2f, 0.3f)

        val entity1 = ContentEntity(
            id = articleId,
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = articleId,
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = dummyEmbedding
        )

        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)

        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 30000L,
                readPercentage = 0.5f
            )
        )
        advanceUntilIdle()

        // Act
        val result = repository.onArticleVisited(ContentId(articleId))

        assertNotNull(result)

        val profile = repository.getUserProfileEmbeddings().first()
        assertNotNull(profile)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_secondVisit_updatesProfile() = runTest {
        // Arrange
        val articleId1 = "article_1"
        val articleId2 = "article_2"
        val emb1 = floatArrayOf(0.1f, 0.2f, 0.3f)
        val emb2 = floatArrayOf(0.4f, 0.5f, 0.6f)

        val entity1 = ContentEntity(
            id = articleId1,
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr1 = ArticleAttributesEntity(
            contentId = articleId1,
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = emb1
        )
        db.contentDao().insertContentUpdateWithDetails(entity1, attr1)

        val entity2 = ContentEntity(
            id = articleId2,
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "science")
        )

        val attr2 = ArticleAttributesEntity(
            contentId = articleId2,
            title = "Alpha Title",
            shortDescription = "Short desc",
            content = "Alpha Content",
            unitEmbedding = emb2
        )
        db.contentDao().insertContentUpdateWithDetails(entity2, attr2)

        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId1,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 30000L,
                readPercentage = 0.5f
            )
        )
        advanceUntilIdle()


        // Act
        repository.onArticleVisited(ContentId(articleId1))

        // Second visit
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId2,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 60000L,
                readPercentage = 0.8f
            )
        )

        // Act
        val result = repository.onArticleVisited(ContentId(articleId2))
        assertNotNull(result)

        // Assert
        val profile = db.userProfileDao().getProfile(USER_ID).first()
        assertNotNull(profile)
        assertEquals(2, profile.visitsCount)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_firstVisit_calculatesCorrectEmbedding() = runTest {
        // Arrange
        val articleId = "article_1"
        val articleEmbedding = floatArrayOf(1.0f, 2.0f, 3.0f)
        val readingTimeMs = 120000L // 2 минуты
        val readPercentage = 0.8f

        setupArticle(articleId, articleEmbedding)

        // Вставляем событие для создания статистики через триггер
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = readingTimeMs,
                readPercentage = readPercentage
            )
        )
        advanceUntilIdle()

        // Act
        val result = repository.onArticleVisited(ContentId(articleId))

        // Assert
        assertNotNull(result)

        // Рассчитываем ожидаемое значение
        val normalizedTime =
            (readingTimeMs / 1000).coerceAtMost(600L).toFloat() / 600f // 120/600 = 0.2f
        val expectedEngagementWeight =
            0.5f * readPercentage + 0.5f * normalizedTime // 0.5 * 0.8 + 0.5 * 0.2 = 0.5f
        val expectedEmbedding = floatArrayOf(
            articleEmbedding[0] * expectedEngagementWeight,
            articleEmbedding[1] * expectedEngagementWeight,
            articleEmbedding[2] * expectedEngagementWeight
        )

        val actualEmbedding = result.value
        assertArraysClose(expectedEmbedding, actualEmbedding)

        // Проверяем профиль в базе
        val profile = db.userProfileDao().getProfile(USER_ID).first()
        assertNotNull(profile)
        assertEquals(1, profile.visitsCount)
        assertArraysClose(expectedEmbedding, profile.embedding)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_secondVisit_calculatesWeightedAverage() = runTest {
        // Arrange
        val articleId1 = "article_1"
        val articleId2 = "article_2"
        val embedding1 = floatArrayOf(2.0f, 4.0f, 6.0f)
        val embedding2 = floatArrayOf(1.0f, 3.0f, 5.0f)

        setupArticle(articleId1, embedding1)
        setupArticle(articleId2, embedding2)

        // Первый визит
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId1,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 60000L, // 1 минута
                readPercentage = 0.6f
            )
        )
        advanceUntilIdle()


        repository.onArticleVisited(ContentId(articleId1))

        // Второй визит
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId2,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 180000L, // 3 минуты
                readPercentage = 0.9f
            )
        )
        advanceUntilIdle()

        // Act
        val result = repository.onArticleVisited(ContentId(articleId2))

        // Assert
        assertNotNull(result)

        // Рассчитываем ожидаемые значения

        // Первый визит:
        val normalizedTime1 = (60000L / 1000).coerceAtMost(600L).toFloat() / 600f // 60/600 = 0.1f
        val engagementWeight1 =
            0.5f * 0.6f + 0.5f * normalizedTime1 // 0.5 * 0.6 + 0.5 * 0.1 = 0.35f
        val firstProfileEmbedding = floatArrayOf(
            embedding1[0] * engagementWeight1,
            embedding1[1] * engagementWeight1,
            embedding1[2] * engagementWeight1
        )

        // Второй визит:
        val normalizedTime2 = (180000L / 1000).coerceAtMost(600L).toFloat() / 600f // 180/600 = 0.3f
        val engagementWeight2 = 0.5f * 0.9f + 0.5f * normalizedTime2 // 0.5 * 0.9 + 0.5 * 0.3 = 0.6f

        // Взвешенное среднее: (старый * count + новый * weight) / (count + 1)
        val expectedEmbedding = floatArrayOf(
            (firstProfileEmbedding[0] * 1f + embedding2[0] * engagementWeight2) / 2f,
            (firstProfileEmbedding[1] * 1f + embedding2[1] * engagementWeight2) / 2f,
            (firstProfileEmbedding[2] * 1f + embedding2[2] * engagementWeight2) / 2f
        )

        val actualEmbedding = result.value
        assertArraysClose(expectedEmbedding, actualEmbedding)

        // Проверяем профиль в базе
        val profile = db.userProfileDao().getProfile(USER_ID).first()
        assertNotNull(profile)
        assertEquals(2, profile.visitsCount)
        assertArraysClose(expectedEmbedding, profile.embedding)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_multipleReadsOfSameArticle_usesUpdatedStats() = runTest {
        // Arrange
        val articleId = "article_1"
        val articleEmbedding = floatArrayOf(1.0f, 1.0f, 1.0f)

        setupArticle(articleId, articleEmbedding)

        // Первое прочтение статьи
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 60000L,
                readPercentage = 0.5f
            )
        )
        advanceUntilIdle()

        // Второе прочтение той же статьи (для обновления статистики)
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 120000L,
                readPercentage = 0.7f
            )
        )
        advanceUntilIdle()


        // Act - посещаем статью после множественных прочтений
        val result = repository.onArticleVisited(ContentId(articleId))

        // Assert
        assertNotNull(result)

        // Проверяем, что статистика обновилась
        val stats = db.articleInteractionStatsDao().getStatsForContent(articleId)
        assertNotNull(stats)
        assertEquals(2, stats.readCount)
        assertEquals(90000.0, stats.avgReadingTime) // (60000 + 120000) / 2
        assertEquals(0.6, stats.avgReadPercentage, 0.001) // (0.5 + 0.7) / 2

        // Рассчитываем ожидаемое эмбеддинг с учетом обновленной статистики
        val normalizedTime = (90L).coerceAtMost(600L).toFloat() / 600f // 90/600 = 0.15f
        val expectedEngagementWeight =
            0.5f * 0.6f + 0.5f * normalizedTime // 0.5 * 0.6 + 0.5 * 0.15 = 0.375f
        val expectedEmbedding = floatArrayOf(
            articleEmbedding[0] * expectedEngagementWeight,
            articleEmbedding[1] * expectedEngagementWeight,
            articleEmbedding[2] * expectedEngagementWeight
        )

        val actualEmbedding = result.value
        assertArraysClose(expectedEmbedding, actualEmbedding)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_zeroEngagement_usesMinimalWeight() = runTest {
        // Arrange
        val articleId = "article_1"
        val articleEmbedding = floatArrayOf(2.0f, 4.0f, 8.0f)

        setupArticle(articleId, articleEmbedding)

        // Вставляем событие с нулевым временем чтения и процентом
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 0L,
                readPercentage = 0.0f
            )
        )
        advanceUntilIdle()


        // Act
        val result = repository.onArticleVisited(ContentId(articleId))

        // Assert
        assertNotNull(result)

        // При отсутствии статистики используются дефолтные значения
        val normalizedTime = (1L).coerceAtMost(600L).toFloat() / 600f // 1/600 ≈ 0.00167f
        val expectedEngagementWeight = 0.5f * 0.001f + 0.5f * normalizedTime // очень малое значение
        val expectedEmbedding = floatArrayOf(
            articleEmbedding[0] * expectedEngagementWeight,
            articleEmbedding[1] * expectedEngagementWeight,
            articleEmbedding[2] * expectedEngagementWeight
        )

        val actualEmbedding = result.value
        assertArraysClose(expectedEmbedding, actualEmbedding)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_maxEngagement_usesFullWeight() = runTest {
        // Arrange
        val articleId = "article_1"
        val articleEmbedding = floatArrayOf(1.0f, 2.0f, 3.0f)

        setupArticle(articleId, articleEmbedding)

        // Максимальное время чтения и полное прочтение
        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 720000L, // 12 минут (больше максимума в 10 минут)
                readPercentage = 1.0f
            )
        )
        advanceUntilIdle()


        // Act
        val result = repository.onArticleVisited(ContentId(articleId))

        // Assert
        assertNotNull(result)

        // Время ограничивается максимумом в 600 секунд
        val normalizedTime = 1.0f // 720/600 обрезается до 1.0f
        val expectedEngagementWeight = 0.5f * 1.0f + 0.5f * normalizedTime // 1.0f
        val expectedEmbedding = floatArrayOf(
            articleEmbedding[0] * expectedEngagementWeight,
            articleEmbedding[1] * expectedEngagementWeight,
            articleEmbedding[2] * expectedEngagementWeight
        )

        val actualEmbedding = result.value
        assertArraysClose(expectedEmbedding, actualEmbedding)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun onArticleVisited_articleWithoutEmbedding_returnsNull() = runTest {
        // Arrange
        val articleId = "article_without_embedding"

        val entity = ContentEntity(
            id = articleId,
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news")
        )

        val attr = ArticleAttributesEntity(
            contentId = articleId,
            title = "Title",
            shortDescription = "Desc",
            content = "Content",
            unitEmbedding = FloatArray(0) // Нет эмбеддинга
        )

        db.contentDao().insertContentUpdateWithDetails(entity, attr)

        db.eventLogDao().insertEvent(
            EventLog(
                contentId = articleId,
                eventType = EventType.READ,
                timestamp = System.currentTimeMillis(),
                readingTimeMillis = 30000L,
                readPercentage = 0.5f
            )
        )
        advanceUntilIdle()

        // Act
        val result = repository.onArticleVisited(ContentId(articleId))

        // Assert
        assertNull(result)
    }

    private suspend fun setupArticle(articleId: String, embedding: FloatArray) {
        val entity = ContentEntity(
            id = articleId,
            type = ContentType.ARTICLE.toString(),
            action = "created",
            updatedAt = DateTimeConvertors.parseIsoToLongMs("2024-01-01T10:00:00"),
            mainImageUrl = "url1",
            tags = listOf("news", "tech")
        )

        val attr = ArticleAttributesEntity(
            contentId = articleId,
            title = "Test Article",
            shortDescription = "Test Description",
            content = "Test Content",
            unitEmbedding = embedding
        )

        db.contentDao().insertContentUpdateWithDetails(entity, attr)
    }

    companion object {
        private fun assertArraysClose(
            expected: FloatArray,
            actual: FloatArray,
            delta: Float = 0.001f
        ) {
            // Check sizes match
            assertWithMessage("Arrays lengths differ")
                .that(actual.size)
                .isEqualTo(expected.size)

            // Element-wise comparison
            for (i in expected.indices) {
                assertWithMessage("Values differ at index $i")
                    .that(actual[i])
                    .isWithin(delta)
                    .of(expected[i])
            }
        }
    }
}