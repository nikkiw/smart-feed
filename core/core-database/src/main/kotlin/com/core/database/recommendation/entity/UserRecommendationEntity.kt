package com.core.database.recommendation.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.core.database.content.entity.ContentEntity

/**
 * Entity representing a user's recommended content item.
 *
 * Each record corresponds to a single recommended content item for a user,
 * identified uniquely by `recommendedContentId`.
 *
 * The `score` field indicates the relevance or strength of this recommendation.
 *
 * The `recommendedContentId` is a foreign key referencing the `ContentEntity` table,
 * ensuring referential integrity and cascading updates/deletions.
 */
@Entity(
    tableName = "user_recommendations",
    foreignKeys = [
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["recommendedContentId"],
            onDelete = ForeignKey.Companion.CASCADE,
            onUpdate = ForeignKey.Companion.CASCADE
        )
    ]
)
data class UserRecommendationEntity(
    @PrimaryKey val recommendedContentId: String,
    val score: Float
)