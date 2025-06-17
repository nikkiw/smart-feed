package com.core.domain.model

/**
 * A value class that wraps a [FloatArray] representing vector embeddings of content.
 *
 * Embeddings are typically used in machine learning and recommendation systems to represent
 * content (e.g., articles) in a numerical vector space, allowing similarity comparisons.
 *
 * @property value The underlying float array containing the embedding values.
 */
@JvmInline
value class Embeddings(val value: FloatArray)
