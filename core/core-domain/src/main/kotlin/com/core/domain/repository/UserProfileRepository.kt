package com.core.domain.repository

import com.core.domain.model.ContentId
import com.core.domain.model.Embeddings
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface responsible for managing user profile embeddings based on interactions,
 * such as visited articles.
 */
interface UserProfileRepository {

    /**
     * Updates the user profile based on a visited article and returns the updated embedding (if available).
     *
     * This method is typically used to personalize recommendations by updating the user's
     * embedding vector using the content of the visited article.
     *
     * @param artileId The ID of the article that the user has visited.
     * @return The updated user profile embeddings, or `null` if the update could not be performed.
     */
    suspend fun onArticleVisited(artileId: ContentId): Embeddings?

    /**
     * Returns a Flow of the current user profile embeddings, if available.
     *
     * Use this to observe changes to the user profile over time. May emit `null`
     * if no profile data is available yet or the user is anonymous.
     *
     * @return A [Flow] containing the latest [Embeddings] representing the user profile, or null.
     */
    suspend fun getUserProfileEmbeddings(): Flow<Embeddings?>
}