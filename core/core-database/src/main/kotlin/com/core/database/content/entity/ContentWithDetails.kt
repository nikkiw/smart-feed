package com.core.database.content.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Composite class for retrieving [ContentEntity] along with its article details.
 *
 * Used in queries where both the core content and its article-specific fields are needed.
 *
 * @property contentUpdate The content entity.
 * @property article The corresponding article attributes, if available.
 */
data class ContentWithDetails(
    @Embedded val contentUpdate: ContentEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "contentId"
    )
    val article: ArticleAttributesEntity?,
)
