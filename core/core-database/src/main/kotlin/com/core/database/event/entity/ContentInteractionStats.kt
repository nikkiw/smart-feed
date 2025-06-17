package com.core.database.event.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing aggregated interaction statistics for an article.
 *
 * This table stores summary data such as:
 * - Total number of times the article was read (`readCount`)
 * - Average reading time in milliseconds (`avgReadingTime`)
 * - Average percentage of the article read (`avgReadPercentage`)
 *
 * The data in this table is automatically updated via a database trigger
 * after every insert into the `event_log` table where the eventType is READ.
 */
@Entity(tableName = "content_interaction_stats")
data class ContentInteractionStats(
    @PrimaryKey val contentId: String,
    val readCount: Int,
    val avgReadingTime: Double, // Average reading time in milliseconds
    val avgReadPercentage: Double // Average read percentage (0.0 to 1.0)
)