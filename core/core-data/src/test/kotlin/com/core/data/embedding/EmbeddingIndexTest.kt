package com.core.data.embedding

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EmbeddingIndexTest {

    @Test
    fun normalize_nonZeroVector_returnsUnitLength() {
        val raw = floatArrayOf(3f, 4f)
        val normalized = EmbeddingIndex.normalize(raw)
        // 3-4-5 triangle => length should be 1
        val length = EmbeddingIndex.dot(normalized, normalized)
        assertThat(length).isWithin(1e-6f).of(1f)
    }

    @Test
    fun normalize_zeroVector_returnsCopy() {
        val raw = floatArrayOf(0f, 0f, 0f)
        val normalized = EmbeddingIndex.normalize(raw)
        assertThat(normalized).isEqualTo(raw)
    }

    @Test
    fun dot_orthogonalVectors_returnsZero() {
        val a = floatArrayOf(1f, 0f)
        val b = floatArrayOf(0f, 1f)
        assertThat(EmbeddingIndex.dot(a, b)).isEqualTo(0f)
    }

    @Test
    fun search_singleNeighbor_returnsMostSimilar() {
        val index = EmbeddingIndex()
        val vecA = EmbeddingIndex.normalize(floatArrayOf(1f, 0f))
        val vecB = EmbeddingIndex.normalize(floatArrayOf(0f, 1f))
        index.add("A", vecA)
        index.add("B", vecB)

        val query = EmbeddingIndex.normalize(floatArrayOf(0.9f, 0.1f))
        val results = index.search(query, k = 1)

        // A should be most similar to query
        assertThat(results).hasSize(1)
        assertThat(results[0].first).isEqualTo("A")
    }

    @Test
    fun search_topK_returnsKResultsSorted() {
        val index = EmbeddingIndex()
        val v1 = EmbeddingIndex.normalize(floatArrayOf(1f, 0f))
        val v2 = EmbeddingIndex.normalize(floatArrayOf(0f, 1f))
        val v3 = EmbeddingIndex.normalize(floatArrayOf(-1f, 0f))
        index.add("1", v1)
        index.add("2", v2)
        index.add("3", v3)

        val query = EmbeddingIndex.normalize(floatArrayOf(1f, 0f))
        val results = index.search(query, k = 2)

        assertThat(results.map { it.first }).containsExactly("1", "2").inOrder()
    }
}
