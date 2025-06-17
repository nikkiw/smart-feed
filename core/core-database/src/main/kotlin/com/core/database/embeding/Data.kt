package com.core.database.embeding


/**
 * Represents an article embedding with its unique ID and associated vector.
 *
 * @property articleId The unique identifier of the article.
 * @property unitEmbedding The embedding vector representing the article content.
 *
 * Equality is based solely on the [articleId], ignoring the embedding values.
 */
data class ArticleEmbedding(
    val articleId: String,
    val unitEmbedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleEmbedding

        return articleId == other.articleId
    }

    override fun hashCode(): Int {
        return articleId.hashCode()
    }
}
