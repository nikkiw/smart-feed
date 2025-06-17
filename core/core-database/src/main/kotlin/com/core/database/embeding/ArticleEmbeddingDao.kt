package com.core.database.embeding

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
    @Query("SELECT contentId AS articleId, unitEmbedding FROM article_attributes")
    suspend fun allEmbeddings(): List<ArticleEmbedding>

    /**
     * Retrieves the embedding vector for a specific article by its ID.
     *
     * @param articleId The unique identifier of the article.
     * @return The [ArticleEmbedding] for the given article ID, or null if not found.
     */
    @Transaction
    @Query("SELECT contentId AS articleId, unitEmbedding FROM article_attributes WHERE contentId = :articleId LIMIT 1")
    suspend fun getEmbeddings(articleId: String): ArticleEmbedding?
}
