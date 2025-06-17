package com.core.database.content.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Many-to-many relationship table between content and tags.
 *
 * Each entry associates a tag name with a content item.
 *
 * The data in this table is automatically maintained by database triggers:
 * - On `INSERT` or `UPDATE` of the `tags` field in the `content` table,
 *   triggers (`trg_update_tags_after_insert`, `trg_update_tags_after_update`)
 *   extract tag values from the JSON array and populate this table.
 *
 * @property contentId ID of the associated content.
 * @property tagName Name of the tag.
 */

@Entity(
    tableName = "content_tags",
    primaryKeys = ["contentId", "tagName"],
    foreignKeys = [
        ForeignKey(
            entity = ContentEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contentId"),
        Index("tagName")
    ]
)
data class ContentTag(
    val contentId: String,
    val tagName: String
)
