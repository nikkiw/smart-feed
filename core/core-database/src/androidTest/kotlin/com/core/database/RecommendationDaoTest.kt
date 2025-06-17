package com.core.database

import android.content.Context
import android.database.SQLException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.content.ContentDao
import com.core.database.content.entity.ContentEntity
import com.core.database.recommendation.RecommendationDao
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.entity.UserRecommendationEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecommendationDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var recommendationDao: RecommendationDao
    private lateinit var contentDao: ContentDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getTestDatabase(context)
        recommendationDao = db.recommendationDao()
        contentDao = db.contentDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    // Helper method to create test content entities
    private suspend fun insertTestContent(contentIds: List<String>) {
        contentIds.forEach { id ->
            val contentEntity = ContentEntity(
                id = id,
                type = "article",
                action = "read",
                updatedAt = System.currentTimeMillis(),
                mainImageUrl = "https://example.com/image_$id.jpg",
                tags = listOf("tag1", "tag2")
            )
            contentDao.insertContentUpdate(contentEntity)
        }
    }

    // User Recommendations Tests
    @Test
    fun insertUserRecommendations_insertsCorrectly() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3")
        insertTestContent(contentIds)

        val recommendations = listOf(
            UserRecommendationEntity("content1", 0.9f),
            UserRecommendationEntity("content2", 0.8f),
            UserRecommendationEntity("content3", 0.7f)
        )

        // When
        recommendationDao.insertUserRecommendations(recommendations)

        // Then
        val result = recommendationDao.getUserRecommendations().first()
        assertThat(result).hasSize(3)
        assertThat(result[0].recommendedContentId).isEqualTo("content1")
        assertThat(result[0].score).isEqualTo(0.9f)
        assertThat(result[1].recommendedContentId).isEqualTo("content2")
        assertThat(result[1].score).isEqualTo(0.8f)
        assertThat(result[2].recommendedContentId).isEqualTo("content3")
        assertThat(result[2].score).isEqualTo(0.7f)
    }

    @Test
    fun getUserRecommendations_returnsOrderedByScoreDesc() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3")
        insertTestContent(contentIds)

        val recommendations = listOf(
            UserRecommendationEntity("content1", 0.5f),
            UserRecommendationEntity("content2", 0.9f),
            UserRecommendationEntity("content3", 0.7f)
        )

        // When
        recommendationDao.insertUserRecommendations(recommendations)

        // Then
        val result = recommendationDao.getUserRecommendations().first()
        assertThat(result).hasSize(3)
        assertThat(result[0].score).isEqualTo(0.9f) // content2
        assertThat(result[1].score).isEqualTo(0.7f) // content3
        assertThat(result[2].score).isEqualTo(0.5f) // content1
    }

    @Test
    fun replaceUserRecommendations_replacesExistingData() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3", "content4")
        insertTestContent(contentIds)

        val initialRecommendations = listOf(
            UserRecommendationEntity("content1", 0.9f),
            UserRecommendationEntity("content2", 0.8f)
        )
        recommendationDao.insertUserRecommendations(initialRecommendations)

        val newRecommendations = listOf(
            UserRecommendationEntity("content3", 0.7f),
            UserRecommendationEntity("content4", 0.6f)
        )

        // When
        recommendationDao.replaceUserRecommendations(newRecommendations)

        // Then
        val result = recommendationDao.getUserRecommendations().first()
        assertThat(result).hasSize(2)
        assertThat(result.map { it.recommendedContentId }).containsExactly("content3", "content4")
        assertThat(result.map { it.score }).containsExactly(0.7f, 0.6f)
    }

    @Test
    fun replaceUserRecommendations_withEmptyList_doesNothing() = runTest {
        // Given
        val contentIds = listOf("content1", "content2")
        insertTestContent(contentIds)

        val initialRecommendations = listOf(
            UserRecommendationEntity("content1", 0.9f),
            UserRecommendationEntity("content2", 0.8f)
        )
        recommendationDao.insertUserRecommendations(initialRecommendations)

        // When
        recommendationDao.replaceUserRecommendations(emptyList())

        // Then
        val result = recommendationDao.getUserRecommendations().first()
        assertThat(result).hasSize(2) // Should remain unchanged
    }

    @Test
    fun deleteUserRecommendations_deletesAllData() = runTest {
        // Given
        val contentIds = listOf("content1", "content2")
        insertTestContent(contentIds)

        val recommendations = listOf(
            UserRecommendationEntity("content1", 0.9f),
            UserRecommendationEntity("content2", 0.8f)
        )
        recommendationDao.insertUserRecommendations(recommendations)

        // When
        recommendationDao.deleteUserRecommendations()

        // Then
        val result = recommendationDao.getUserRecommendations().first()
        assertThat(result).isEmpty()
    }

    // Content Recommendations Tests
    @Test
    fun insertContentRecommendations_insertsCorrectly() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3")
        insertTestContent(contentIds)

        val recommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.9f),
            ContentRecommendationEntity("content1", "content3", 0.8f)
        )

        // When
        recommendationDao.insertContentRecommendations(recommendations)

        // Then
        val result = recommendationDao.getContentRecommendations("content1").first()
        assertThat(result).hasSize(2)
        assertThat(result[0].recommendedContentId).isEqualTo("content2")
        assertThat(result[0].score).isEqualTo(0.9f)
        assertThat(result[1].recommendedContentId).isEqualTo("content3")
        assertThat(result[1].score).isEqualTo(0.8f)
    }

    @Test
    fun getContentRecommendations_returnsOrderedByScoreDesc() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3", "content4")
        insertTestContent(contentIds)

        val recommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.5f),
            ContentRecommendationEntity("content1", "content3", 0.9f),
            ContentRecommendationEntity("content1", "content4", 0.7f)
        )

        // When
        recommendationDao.insertContentRecommendations(recommendations)

        // Then
        val result = recommendationDao.getContentRecommendations("content1").first()
        assertThat(result).hasSize(3)
        assertThat(result[0].score).isEqualTo(0.9f) // content3
        assertThat(result[1].score).isEqualTo(0.7f) // content4
        assertThat(result[2].score).isEqualTo(0.5f) // content2
    }

    @Test
    fun getContentRecommendations_filtersCorrectContentId() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3", "content4")
        insertTestContent(contentIds)

        val recommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.9f),
            ContentRecommendationEntity("content1", "content3", 0.8f),
            ContentRecommendationEntity("content2", "content4", 0.7f)
        )

        // When
        recommendationDao.insertContentRecommendations(recommendations)

        // Then
        val result1 = recommendationDao.getContentRecommendations("content1").first()
        val result2 = recommendationDao.getContentRecommendations("content2").first()

        assertThat(result1).hasSize(2)
        assertThat(result1.map { it.recommendedContentId }).containsExactly("content2", "content3")

        assertThat(result2).hasSize(1)
        assertThat(result2[0].recommendedContentId).isEqualTo("content4")
    }

    @Test
    fun replaceContentRecommendations_replacesExistingForSameContent() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3", "content4")
        insertTestContent(contentIds)

        val initialRecommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.9f),
            ContentRecommendationEntity("content1", "content3", 0.8f)
        )
        recommendationDao.insertContentRecommendations(initialRecommendations)

        val newRecommendations = listOf(
            ContentRecommendationEntity("content1", "content4", 0.7f)
        )

        // When
        recommendationDao.replaceContentRecommendations(newRecommendations)

        // Then
        val result = recommendationDao.getContentRecommendations("content1").first()
        assertThat(result).hasSize(1)
        assertThat(result[0].recommendedContentId).isEqualTo("content4")
        assertThat(result[0].score).isEqualTo(0.7f)
    }

    @Test
    fun replaceContentRecommendations_withEmptyList_doesNothing() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3")
        insertTestContent(contentIds)

        val initialRecommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.9f),
            ContentRecommendationEntity("content1", "content3", 0.8f)
        )
        recommendationDao.insertContentRecommendations(initialRecommendations)

        // When
        recommendationDao.replaceContentRecommendations(emptyList())

        // Then
        val result = recommendationDao.getContentRecommendations("content1").first()
        assertThat(result).hasSize(2) // Should remain unchanged
    }

    @Test
    fun deleteContentRecommendationsForContent_deletesOnlySpecificContent() = runTest {
        // Given
        val contentIds = listOf("content1", "content2", "content3", "content4")
        insertTestContent(contentIds)

        val recommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.9f),
            ContentRecommendationEntity("content1", "content3", 0.8f),
            ContentRecommendationEntity("content2", "content4", 0.7f)
        )
        recommendationDao.insertContentRecommendations(recommendations)

        // When
        recommendationDao.deleteContentRecommendationsForContent("content1")

        // Then
        val result1 = recommendationDao.getContentRecommendations("content1").first()
        val result2 = recommendationDao.getContentRecommendations("content2").first()

        assertThat(result1).isEmpty()
        assertThat(result2).hasSize(1)
        assertThat(result2[0].recommendedContentId).isEqualTo("content4")
    }

    @Test
    fun onConflictReplace_updatesExistingRecords() = runTest {
        // Given
        val contentIds = listOf("content1", "content2")
        insertTestContent(contentIds)

        val initialRecommendation = UserRecommendationEntity("content1", 0.5f)
        recommendationDao.insertUserRecommendations(listOf(initialRecommendation))

        val updatedRecommendation = UserRecommendationEntity("content1", 0.9f)

        // When
        recommendationDao.insertUserRecommendations(listOf(updatedRecommendation))

        // Then
        val result = recommendationDao.getUserRecommendations().first()
        assertThat(result).hasSize(1)
        assertThat(result[0].score).isEqualTo(0.9f)
    }

    @Test
    fun flowUpdates_emitWhenDataChanges() = runTest {
        val results = mutableListOf<List<UserRecommendationEntity>>()
        val flow = recommendationDao.getUserRecommendations()

        val job = launch {
            flow.take(3).collect { results.add(it) }
        }

        // Given
        val contentIds = listOf("content1", "content2")
        insertTestContent(contentIds)

        // When
        delay(100) // Wait for initial empty emit

        val firstRecommendations = listOf(UserRecommendationEntity("content1", 0.9f))
        recommendationDao.insertUserRecommendations(firstRecommendations)

        delay(100)

        val secondRecommendations = listOf(
            UserRecommendationEntity("content1", 0.9f),
            UserRecommendationEntity("content2", 0.8f)
        )
        recommendationDao.insertUserRecommendations(secondRecommendations)

        job.join()

        // Then
        assertThat(results).hasSize(3)
        assertThat(results[0]).isEmpty() // Initial empty state
        assertThat(results[1]).hasSize(2) // After first insert
        assertThat(results[2]).hasSize(2) // After second insert
    }

    @Test
    fun foreignKeyConstraint_preventsOrphanedRecommendations() = runTest {
        // Given - trying to insert recommendation without existing content
        val orphanedRecommendation = UserRecommendationEntity("nonexistent_content", 0.9f)

        // When & Then - should throw constraint exception
        assertThrows<SQLException>(SQLException::class.java) {
            runBlocking {
                recommendationDao.insertUserRecommendations(listOf(orphanedRecommendation))
            }
        }
    }

    @Test
    fun cascadeDelete_removesRecommendationsWhenContentDeleted() = runTest {
        // Given
        val contentIds = listOf("content1", "content2")
        insertTestContent(contentIds)

        val userRecommendations = listOf(
            UserRecommendationEntity("content1", 0.9f),
            UserRecommendationEntity("content2", 0.8f)
        )
        recommendationDao.insertUserRecommendations(userRecommendations)

        val contentRecommendations = listOf(
            ContentRecommendationEntity("content1", "content2", 0.9f)
        )
        recommendationDao.insertContentRecommendations(contentRecommendations)

        // When - delete content
        contentDao.deleteContentUpdateById("content1")

        // Then - related recommendations should be deleted due to CASCADE
        val userResult = recommendationDao.getUserRecommendations().first()
        val contentResult = recommendationDao.getContentRecommendations("content1").first()

        assertThat(userResult).hasSize(1) // Only content2 recommendation remains
        assertThat(userResult[0].recommendedContentId).isEqualTo("content2")
        assertThat(contentResult).isEmpty() // All content1 recommendations deleted
    }
}