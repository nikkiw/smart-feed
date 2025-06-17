package com.core.database.recommendation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.core.database.content.entity.ContentEntity

/**
 * Entity representing a recommendation relationship between two content items.
 *
 * Each record links a content item (`contentId`) to a recommended content item (`recommendedContentId`)
 * along with a relevance score (`score`) that quantifies the strength or quality of the recommendation.
 *
 * The composite primary key is formed by the combination of `contentId` and `recommendedContentId`.
 *
 * Foreign keys enforce referential integrity to the `content` table,
 * with cascading updates and deletions to maintain consistency.
 *
 * An index on `recommendedContentId` is created to optimize queries filtering by recommended content.
 */
@Entity(
    tableName = "content_recommendations",
    primaryKeys = ["contentId", "recommendedContentId"],
    foreignKeys = [
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["recommendedContentId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recommendedContentId"]),
    ]
)
data class ContentRecommendationEntity(
    val contentId: String,
    val recommendedContentId: String,
    val score: Float
)
