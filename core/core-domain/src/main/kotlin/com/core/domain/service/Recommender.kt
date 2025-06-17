package com.core.domain.service

/**
 * Interface defining methods for updating recommendations for users and articles.
 *
 * This can be used to trigger background updates of recommendation models or vectors,
 * e.g. based on new content, user interactions, or changes in embeddings.
 */
interface Recommender {

    /**
     * Updates personalized recommendations for the current user.
     *
     * This method is typically called when user behavior data (e.g., visited articles)
     * has changed and the recommendation model needs to be re-evaluated.
     */
    suspend fun updateRecommendationsForUser()

    /**
     * Updates recommendations for all or selected articles.
     *
     * This method is typically used when new content is added or embeddings are updated,
     * requiring recomputation of related content (e.g., "You might also like") suggestions.
     */
    suspend fun updateRecommendationsForArticles()
}