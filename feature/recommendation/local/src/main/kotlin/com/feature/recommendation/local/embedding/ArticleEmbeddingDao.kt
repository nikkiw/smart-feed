package com.feature.recommendation.local.embedding

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/**
 * DAO for accessing article embeddings stored in the database.
 */
@Dao
interface ArticleEmbeddingDao {
    /**
     * Retrieves all article embeddings from the database.
     *
     * @return A list of [ArticleEmbedding] containing article IDs and their embedding vectors.
     */
    @Transaction
    @Query(
        """
        SELECT a.contentId AS articleId, a.unitEmbedding, c.languageCode
        FROM article_attributes a
        JOIN content c ON c.id = a.contentId
        """,
    )
    suspend fun allEmbeddings(): List<ArticleEmbedding>

    /**
     * Retrieves the embedding vector for a specific article by its ID.
     *
     * @param articleId The unique identifier of the article.
     * @return The [ArticleEmbedding] for the given article ID, or null if not found.
     */
    @Transaction
    @Query(
        """
        SELECT a.contentId AS articleId, a.unitEmbedding, c.languageCode
        FROM article_attributes a
        JOIN content c ON c.id = a.contentId
        WHERE a.contentId = :articleId
        LIMIT 1
        """,
    )
    suspend fun getEmbeddings(articleId: String): ArticleEmbedding?
}
