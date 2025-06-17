package com.core.math

import kotlin.math.sqrt

/**
 * Utility object for operating on embedding vectors.
 */
object Embeddings {
    /**
     * Computes the cosine similarity between two embedding vectors.
     *
     * The cosine similarity is defined as the dot product of the vectors
     * divided by the product of their magnitudes.
     * Both vectors must have the same length.
     *
     * @param a The first embedding vector.
     * @param b The second embedding vector.
     * @return The cosine similarity value in the range [-1.0, 1.0].
     * @throws IllegalArgumentException if the vectors differ in size.
     */
    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        require(a.size == b.size) { "Embedding vectors must have the same length." }
        var dot = 0f
        var normA = 0f
        var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        return dot / (sqrt(normA) * sqrt(normB))
    }
}