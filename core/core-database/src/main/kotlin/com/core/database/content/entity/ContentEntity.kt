package com.core.database.content.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Entity representing a general piece of content (e.g., article, etc.).
 *
 * This table stores the core metadata of any content item.
 *
 * @property id Unique identifier of the content.
 * @property type Content type (e.g., "article").
 * @property action Describes what happened to the content (e.g., "upsert", "delete").
 * @property updatedAt Last update timestamp in milliseconds.
 * @property mainImageUrl URL of the main visual associated with the content.
 * @property tags Semantic tags or keywords associated with the content.
 */
@Entity(
    tableName = "content",
    indices = [
        Index(value = ["type"]),
        Index(value = ["updatedAt"])
    ]
)
data class ContentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val action: String,
    val updatedAt: Long,
    val mainImageUrl: String,
    @TypeConverters(Converter::class)
    val tags: List<String>
)
