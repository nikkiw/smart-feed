package com.feature.recommendation.local.event

import androidx.room.Dao
import androidx.room.Query
import com.feature.recommendation.local.event.entity.ContentInteractionStats

/**
 * DAO interface for accessing and managing article interaction statistics.
 *
 * Provides methods to query statistics about how users interact with articles,
 * such as reading counts and average reading times.
 */
@Dao
interface ContentInteractionStatsDao {
    /**
     * Retrieves a list of all distinct article IDs that have recorded reading statistics.
     *
     * @return List of article IDs with interaction stats.
     */
    @Query("SELECT DISTINCT contentId FROM content_interaction_stats")
    suspend fun getAllReadContentIds(): List<String>

    /**
     * Retrieves a list of the top articles sorted by the number of reads in descending order.
     * The number of articles returned is limited by the [limit] parameter.
     *
     * @param limit The maximum number of articles to return.
     * @return List of [ContentInteractionStats] for the top articles by read count.
     */
    @Query("SELECT * FROM content_interaction_stats ORDER BY readCount DESC LIMIT :limit")
    suspend fun getTopContentByReadCount(limit: Int): List<ContentInteractionStats>

    @Query(
        """
        SELECT c.languageCode
        FROM content_interaction_stats s
        JOIN content c ON c.id = s.contentId
        WHERE c.languageCode != :unknownLanguage
        ORDER BY s.readCount DESC, s.avgReadPercentage DESC, s.avgReadingTime DESC
        LIMIT :limit
        """,
    )
    suspend fun getTopReadLanguageCodes(limit: Int, unknownLanguage: String = "und"): List<String>

    /**
     * Retrieves the interaction statistics for a specific article identified by its ID.
     *
     * @param contentId The unique identifier of the article.
     * @return [ContentInteractionStats] object if found; null otherwise.
     */
    @Query("SELECT * FROM content_interaction_stats WHERE contentId = :contentId LIMIT 1")
    suspend fun getStatsForContent(contentId: String): ContentInteractionStats?
}
