package com.core.data.repository

import androidx.annotation.VisibleForTesting
import com.core.database.embeding.ArticleEmbeddingDao
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.userprofile.UserProfileDao
import com.core.database.userprofile.UserProfileEntity
import com.core.di.IoDispatcher
import com.core.domain.model.ContentId
import com.core.domain.model.Embeddings
import com.core.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject


@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val USER_ID = 1L

/**
 * Implementation of [UserProfileRepository] that manages user profile embeddings based on article interactions.
 *
 * This repository builds and maintains a user profile by analyzing article reading behavior and creating
 * weighted embeddings that represent user interests and preferences over time.
 *
 * ## Algorithm Overview
 * 1. **Engagement Calculation**: Combines reading percentage and time spent to calculate engagement weight
 * 2. **Profile Building**: For new users, creates initial profile from first article interaction
 * 3. **Profile Evolution**: For existing users, updates profile using weighted moving average
 * 4. **Thread Safety**: Uses mutex to ensure atomic updates to user profile
 *
 * ## Usage Examples
 * ```kotlin
 * // Track article visit and update user profile
 * val repository = UserProfileRepositoryImpl(embeddingDao, statsDao, profileDao, ioDispatcher)
 *
 * // Update profile when user visits an article
 * val updatedEmbeddings = repository.onArticleVisited(ContentId("article123"))
 *
 * // Get current user profile embeddings
 * repository.getUserProfileEmbeddings().collect { embeddings ->
 *     embeddings?.let {
 *         // Update content recommendation for user by recommender
 *          recommender.updateRecommendationsForUser()
 *     }
 * }
 * ```
 *
 * @param embeddingDao DAO for accessing article embeddings
 * @param contentInteractionStatsDao DAO for accessing article interaction statistics
 * @param userProfileDao DAO for managing user profile data
 * @param ioDispatcher Coroutine dispatcher for I/O operations
 */
class UserProfileRepositoryImpl @Inject constructor(
    private val embeddingDao: ArticleEmbeddingDao,
    private val contentInteractionStatsDao: ContentInteractionStatsDao,
    private val userProfileDao: UserProfileDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserProfileRepository {

    // Mutex ensures thread-safe updates to user profile to prevent race conditions
    // when multiple articles are visited simultaneously
    private val mutex = Mutex()

    /**
     * Processes an article visit and updates the user profile based on engagement metrics.
     *
     * This method calculates user engagement based on reading statistics and updates the user's
     * embedding profile using a weighted moving average approach.
     *
     * ## Algorithm Steps:
     * 1. Retrieve article embeddings and interaction statistics
     * 2. Calculate engagement weight from reading percentage and time
     * 3. For new users: Create initial profile with weighted embeddings
     * 4. For existing users: Update profile using weighted moving average
     *
     * @param artileId The unique identifier of the visited article
     * @return Updated user embeddings after processing the visit, or null if article has no embeddings
     *
     * @sample
     * ```kotlin
     * val contentId = ContentId("tech_article_456")
     * val updatedProfile = repository.onArticleVisited(contentId)
     * updatedProfile?.let { embeddings ->
     *     // Profile successfully updated with new article interaction
     *     analyticsService.trackProfileUpdate(embeddings)
     * }
     * ```
     */
    override suspend fun onArticleVisited(artileId: ContentId): Embeddings? =
        withContext(ioDispatcher) {
            mutex.withLock {
                // Retrieve article embeddings - return null if article has no embeddings
                embeddingDao.getEmbeddings(artileId.value)?.unitEmbedding?.let { articleEmbeddings ->
                    if (articleEmbeddings.isEmpty())
                        return@let null

                    // Get interaction statistics for engagement calculation
                    val stats = contentInteractionStatsDao.getStatsForContent(artileId.value)

                    // Calculate engagement percentage (minimum 0.1% to avoid zero weights)
                    val engPercent = (stats?.avgReadPercentage ?: 0.001).coerceAtLeast(0.001)

                    // Normalize reading time to 0-1 scale (1-600 seconds range)
                    val engTime = normalizeTime(
                        ((stats?.avgReadingTime ?: 1000).toLong() / 1000)
                    )

                    // Combine reading percentage and time with equal weighting (50% each)
                    val engagementWeight = (0.5f * engPercent + 0.5f * engTime).toFloat()

                    // Retrieve existing user profile
                    val userProfile = userProfileDao.getProfile(USER_ID).first()

                    val userEmbedding = if (userProfile == null) {
                        // First-time user: Initialize profile with weighted article embeddings
                        val weightedEmb = FloatArray(articleEmbeddings.size) { i ->
                            articleEmbeddings[i] * engagementWeight
                        }
                        userProfileDao.upsert(UserProfileEntity(USER_ID, weightedEmb, 1))
                        weightedEmb
                    } else {
                        // Existing user: Update profile using weighted moving average
                        // Formula: new_embedding = (old_embedding * visit_count + article_embedding * engagement) / (visit_count + 1)
                        val oldEmb = userProfile.embedding
                        val count = userProfile.visitsCount.toFloat()
                        val newEmb = FloatArray(oldEmb.size) { i ->
                            (oldEmb[i] * count + articleEmbeddings[i] * engagementWeight) / (count + 1f)
                        }
                        userProfileDao.upsert(
                            UserProfileEntity(
                                USER_ID,
                                newEmb,
                                userProfile.visitsCount + 1
                            )
                        )
                        newEmb
                    }
                    Embeddings(userEmbedding)
                }
            }
        }

    /**
     * Retrieves the current user profile embeddings as a reactive stream.
     *
     * Returns a Flow that emits the latest user profile embeddings whenever the profile
     * is updated. This allows UI components and recommendation systems to react to profile changes.
     *
     * @return Flow emitting current user embeddings, or null if no profile exists
     *
     * @sample
     * ```kotlin
     * // Observe profile changes for real-time recommendations
     * repository.getUserProfileEmbeddings()
     *     .filterNotNull()
     *     .collect { embeddings ->
     *          recommender.updateRecommendationsForUser()
     *     }
     * ```
     */
    override suspend fun getUserProfileEmbeddings(): Flow<Embeddings?> =
        userProfileDao.getProfile(USER_ID).map { profile ->
            if (profile == null) {
                null
            } else {
                Embeddings(profile.embedding)
            }
        }.flowOn(ioDispatcher)

    /**
     * Normalizes reading time to a 0-1 scale for engagement calculation.
     *
     * Uses linear normalization to convert reading time in seconds to a normalized score.
     * Times below minimum are clipped to minimum, times above maximum are clipped to maximum.
     *
     * @param seconds Reading time in seconds
     * @param minSeconds Minimum meaningful reading time (default: 1 second)
     * @param maxSeconds Maximum considered reading time (default: 600 seconds / 10 minutes)
     * @return Normalized time score between 0.0 and 1.0
     *
     * @sample
     * ```kotlin
     * val shortRead = normalizeTime(30)    // Returns ~0.05 (30/600)
     * val longRead = normalizeTime(300)    // Returns 0.5 (300/600)
     * val veryLongRead = normalizeTime(800) // Returns 1.0 (clipped to maxSeconds)
     * ```
     */
    private fun normalizeTime(
        seconds: Long,
        minSeconds: Long = 1L,
        maxSeconds: Long = 600L
    ): Float {
        // Linear normalization with clipping to ensure result is always in [0, 1] range
        return (seconds.coerceAtLeast(minSeconds).coerceAtMost(maxSeconds).toFloat() / maxSeconds)
    }
}