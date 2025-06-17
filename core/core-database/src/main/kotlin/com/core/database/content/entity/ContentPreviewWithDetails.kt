package com.core.database.content.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Projection-based data class to retrieve content and its article preview attributes.
 *
 * Optimized for list screens, cards, or other places where full article content is not required.
 *
 * @property contentUpdate Base content metadata.
 * @property article Preview of article attributes (title and short description).
 */
data class ContentPreviewWithDetails(
    @Embedded val contentUpdate: ContentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "contentId",
        entity = ArticleAttributesEntity::class,
        projection = ["contentId", "title", "shortDescription"]
    )
    val article: ArticlePreviewAttributes?
)
