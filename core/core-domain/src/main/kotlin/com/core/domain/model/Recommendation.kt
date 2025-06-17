package com.core.domain.model

/**
 * Represents a recommendation for a content item, typically based on relevance or user preferences.
 *
 * @property articleId The unique identifier of the recommended content item.
 * @property score A float in range [0.0, 1.0] representing recommendation confidence.
 *   1.0 means highly confident/relevant, 0.0 means least relevant.
 */
data class Recommendation(
    val articleId: ContentId,
    val score: Float
)
