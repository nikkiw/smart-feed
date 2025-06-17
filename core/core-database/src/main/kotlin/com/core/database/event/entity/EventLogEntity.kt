package com.core.database.event.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Types of user interaction events related to articles.
 */
enum class EventType {
    IMPRESSION,     // Article impression (view)
    CLICK,          // Article clicked
    READ,           // Started reading
//    READ_COMPLETE,  // Finished reading
//    BOOKMARK,       // Added to bookmarks/favorites
//    SHARE,          // Shared article
//    HIDE            // Article hidden
}

/**
 * Represents a log entry for a user event related to an article.
 *
 * @property id Auto-generated unique identifier for the event log entry.
 * @property contentId Identifier of the article related to the event.
 * @property eventType Type of the event.
 * @property timestamp Event timestamp in milliseconds since epoch. Defaults to current time.
 * @property readingTimeMillis Time spent reading the article in milliseconds, if applicable.
 * @property readPercentage Percentage of the article read (range 0.0 to 1.0), if applicable.
 */
@Entity(tableName = "event_log")
data class EventLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentId: String,
    val eventType: EventType,
    val timestamp: Long = System.currentTimeMillis(),
    val readingTimeMillis: Long? = null,
    val readPercentage: Float? = null
)


