package com.core.database.recommendation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.entity.UserRecommendationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO interface for managing user and content recommendations in the database.
 *
 * Provides methods to insert, replace, query, and delete recommendations.
 *
 * User recommendations represent personalized suggestions for a user.
 * Content recommendations represent related or suggested content items linked to a specific content ID.
 */
@Dao
interface RecommendationDao {

    /**
     * Replace all user recommendations.
     *
     * Deletes all existing user recommendations and inserts the provided list.
     * If the provided list is empty, no operation is performed.
     *
     * @param recommendations list of [UserRecommendationEntity] to be inserted
     */
    @Transaction
    suspend fun replaceUserRecommendations(recommendations: List<UserRecommendationEntity>) {
        if (recommendations.isEmpty()) return
        deleteUserRecommendations()
        insertUserRecommendations(recommendations)
    }

    /**
     * Insert a list of user recommendations.
     *
     * On conflict, existing records will be replaced.
     *
     * @param recommendations list of [UserRecommendationEntity] to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserRecommendations(recommendations: List<UserRecommendationEntity>)

    /**
     * Get all user recommendations ordered by descending score.
     *
     * @return a [Flow] emitting lists of [UserRecommendationEntity]
     */
    @Query("SELECT * FROM user_recommendations ORDER BY score DESC")
    fun getUserRecommendations(): Flow<List<UserRecommendationEntity>>

    /**
     * Delete all user recommendations.
     */
    @Query("DELETE FROM user_recommendations")
    suspend fun deleteUserRecommendations()


    /**
     * Replace all content recommendations for a specific content ID.
     *
     * Deletes existing recommendations for the content and inserts the new list.
     * If the list is empty, no operation is performed.
     *
     * @param recommendations list of [ContentRecommendationEntity] to be inserted
     */
    @Transaction
    suspend fun replaceContentRecommendations(recommendations: List<ContentRecommendationEntity>) {
        if (recommendations.isEmpty()) return
        val contentId = recommendations.first().contentId
        deleteContentRecommendationsForContent(contentId)
        insertContentRecommendations(recommendations)
    }

    /**
     * Insert a list of content recommendations.
     *
     * On conflict, existing records will be replaced.
     *
     * @param recommendations list of [ContentRecommendationEntity] to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentRecommendations(recommendations: List<ContentRecommendationEntity>)

    /**
     * Get content recommendations for a given content ID ordered by descending score.
     *
     * @param contentId the content ID for which to fetch recommendations
     * @return a [Flow] emitting lists of [ContentRecommendationEntity]
     */
    @Query("SELECT * FROM content_recommendations WHERE contentId = :contentId ORDER BY score DESC")
    fun getContentRecommendations(contentId: String): Flow<List<ContentRecommendationEntity>>

    /**
     * Delete all content recommendations for a given content ID.
     *
     * @param contentId the content ID for which to delete recommendations
     */
    @Query("DELETE FROM content_recommendations WHERE contentId = :contentId")
    suspend fun deleteContentRecommendationsForContent(contentId: String)
}
