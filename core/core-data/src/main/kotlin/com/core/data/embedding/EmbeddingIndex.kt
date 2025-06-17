package com.core.data.embedding

import com.core.database.embeding.ArticleEmbedding
import java.util.PriorityQueue
import kotlin.math.sqrt

typealias ArticleScore = Pair<String, Float>

/**
 * An index for storing normalized embeddings and performing k-nearest neighbor search.
 *
 * Embeddings are normalized using the L2 norm to ensure that cosine similarity
 * corresponds to a simple dot product between unit-length vectors. L2 normalization
 * is chosen because it preserves angles between vectors (i.e., direction) while
 * removing the influence of magnitude, making it ideal for semantic similarity measures.
 *
 * This class allows adding raw embeddings (which should be pre-normalized),
 * retrieving stored embeddings by article ID, and searching for the top-k most
 * similar articles given a query vector.
 *
 * Usage example:
 * ```kotlin
 * // Create an index
 * val index = EmbeddingIndex()
 *
 * // Prepare raw vectors for articles
 * val rawA = floatArrayOf(0.1f, 0.9f, 0.3f)
 * val rawB = floatArrayOf(0.8f, 0.2f, 0.1f)
 * // Normalize vectors using companion helper (L2 norm)
 * val unitA = EmbeddingIndex.normalize(rawA)
 * val unitB = EmbeddingIndex.normalize(rawB)
 *
 * // Add embeddings to the index
 * index.add("articleA", unitA)
 * index.add("articleB", unitB)
 *
 * // Perform a search: find top-1 neighbor for a query vector
 * val query = EmbeddingIndex.normalize(floatArrayOf(0.5f, 0.4f, 0.7f))
 * val topResults: List<ArticleScore> = index.search(query, k = 1)
 * println("Top result: ${topResults[0].first} with similarity ${topResults[0].second}")
 * ```
 */
class EmbeddingIndex {
    // Internal storage of embeddings: list of ArticleEmbedding(id, unitVector)
    private val embeddings = mutableListOf<ArticleEmbedding>()

    /**
     * Adds a normalized embedding to the index under the given article ID.
     *
     * @param id Unique identifier for the article.
     * @param unitVector The already-normalized embedding vector (unit length). If not normalized,
     *                   consider calling [normalize] first to ensure correct similarity.
     */
    fun add(id: String, unitVector: FloatArray) {
        embeddings.add(ArticleEmbedding(id, unitVector))
    }

    /**
     * Retrieves the normalized embedding vector for the specified article ID.
     *
     * @param id The article ID to lookup.
     * @return The unit-length embedding vector associated with the article.
     * @throws NoSuchElementException if the ID is not found.
     */
    fun get(id: String): FloatArray {
        return embeddings.first { it.articleId == id }.unitEmbedding
    }

    /**
     * Searches for the top-k most similar articles to the query vector using cosine similarity.
     *
     * Internally uses a min-heap (priority queue) of size k to track the k highest similarity scores.
     * Cosine similarity between unit vectors reduces to their dot product, so using L2-normalized
     * embeddings allows for efficient similarity computation.
     * This approach achieves O(n log k) time complexity.
     *
     * @param queryUnit A normalized query vector (unit length).
     * @param k Number of nearest neighbors to retrieve.
     * @return A list of pairs (contentId, similarityScore), sorted descending by similarity.
     */
    fun search(queryUnit: FloatArray, k: Int): List<ArticleScore> {

        // Min-heap to store top-k results; root has the smallest similarity in the heap
        val pq = PriorityQueue<ArticleScore>(
            /* initialCapacity = */ k.coerceAtLeast(1),
            Comparator { o1, o2 -> o1.second.compareTo(o2.second) }
        )

        for (emb in embeddings) {
            // Compute cosine similarity = dot product of unit vectors
            val sim = dot(queryUnit, emb.unitEmbedding)
            if (pq.size < k) {
                // Heap not full, add current pair
                pq.add(emb.articleId to sim)
            } else {
                // If current similarity is greater than the smallest in heap
                val top = pq.peek()
                if (top != null && sim > top.second) {
                    pq.poll() // Remove the smallest
                    pq.add(emb.articleId to sim)
                }
            }
        }

        // Convert heap to list and sort descending by similarity
        return pq.toList()
            .sortedByDescending { it.second }
    }

    companion object {
        /**
         * Normalizes a vector by its L2 norm (Euclidean length). If the vector is zero,
         * returns a copy unchanged. L2 norm is chosen because it produces unit vectors
         * where cosine similarity corresponds directly to dot product, emphasizing direction
         * over magnitude.
         *
         * @param vec The raw vector to normalize.
         * @return A new vector of the same length with unit L2 norm.
         */
        fun normalize(vec: FloatArray): FloatArray {
            var sumSq = 0f
            for (x in vec) sumSq += x * x
            val norm = sqrt(sumSq)
            if (norm == 0f) return vec.copyOf()
            return FloatArray(vec.size) { i -> vec[i] / norm }
        }

        /**
         * Returns the element-wise negation of a vector.
         * Useful for finding the "opposite" direction in embedding space.
         *
         * @param vec The input vector (assumed normalized if used for similarity).
         * @return A new vector where each element is negated.
         */
        fun opposite(vec: FloatArray): FloatArray {
            return FloatArray(vec.size) { i -> -vec[i] }
        }

        /**
         * Computes the dot product (scalar product) of two same-length vectors.
         *
         * @param a First vector.
         * @param b Second vector.
         * @return The dot product sum(a[i] * b[i]).
         */
        fun dot(a: FloatArray, b: FloatArray): Float {
            var sum = 0f
            for (i in a.indices) sum += a[i] * b[i]
            return sum
        }
    }
}