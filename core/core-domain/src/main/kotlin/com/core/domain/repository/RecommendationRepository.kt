package com.core.domain.repository

import com.core.domain.model.ContentId
import com.core.domain.model.Recommendation
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface responsible for providing content recommendations.
 *
 * This interface defines methods to retrieve recommendations either for a user
 * (personalized or general) or for a specific article (related content).
 */
interface RecommendationRepository {

    /**
     * Returns a Flow of recommended content items for the current or specified user.
     *
     * The list of [Recommendation] objects typically includes content IDs and relevance scores.
     * This method is usually used for personalized recommendations based on user preferences or behavior.
     *
     * @return A [Flow] containing a list of [Recommendation] objects.
     */
    fun recommendForUser(): Flow<List<Recommendation>>

    /**
     * Returns a list of recommended content items related to a specific article.
     *
     * These recommendations are often based on similarity (e.g., embeddings, tags, etc.)
     * and can be used to show "You might also like" suggestions in the UI.
     *
     * @param contentId The ID of the article for which to get related recommendations.
     * @return A list of [Recommendation] objects.
     */
    suspend fun recommendForArticle(contentId: ContentId): List<Recommendation>
}