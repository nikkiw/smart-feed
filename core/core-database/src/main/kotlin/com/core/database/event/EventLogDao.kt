package com.core.database.event

import androidx.room.Dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.core.database.event.entity.EventLog
import com.core.database.event.entity.EventType
import kotlinx.coroutines.flow.Flow

/**
 * DAO interface for managing event logs related to article interactions.
 *
 * Provides methods to insert event records (e.g., impressions, clicks, reads)
 * and to query event data for analytics, reporting, and user behavior tracking.
 *
 * Features include:
 * - Inserting single or multiple events with conflict handling.
 * - Retrieving all events for a specific article, ordered by timestamp.
 * - Retrieving recent events across all articles.
 * - Counting events of a specific type per article to measure engagement.
 */
@Dao
interface EventLogDao {

    /**
     * Inserts a single event into the database.
     * If a conflict occurs (duplicate), the event is ignored.
     *
     * @param event The [EventLog] entity to insert.
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvent(event: EventLog)

    /**
     * Inserts multiple events into the database.
     * Conflicting events are ignored.
     *
     * @param events List of [EventLog] entities to insert.
     */
    @Transaction
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEvents(events: List<EventLog>)

    /**
     * Retrieves all events associated with a given article, ordered by descending timestamp.
     * Useful for analytics or viewing the event history of the article.
     *
     * @param contentId The ID of the article to fetch events for.
     * @return A [Flow] emitting lists of [EventLog] entities for the article.
     */
    @Query("SELECT * FROM event_log WHERE contentId = :contentId ORDER BY timestamp DESC")
    fun getEventsForContent(contentId: String): Flow<List<EventLog>>

    /**
     * Retrieves the most recent events across all articles, limited by [limit].
     * Useful for debugging or displaying recent user activity.
     *
     * @param limit The maximum number of events to retrieve.
     * @return A [Flow] emitting lists of recent [EventLog] entities.
     */
    @Query("SELECT * FROM event_log ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<EventLog>>

    /**
     * Counts the number of events of a specific [eventType] for a given article.
     * Can be used to track engagement metrics such as total impressions or clicks.
     *
     * @param contentId The ID of the article.
     * @param eventType The type of event to count.
     * @return The count of matching events as an integer.
     */
    @Query("SELECT COUNT(*) FROM event_log WHERE contentId = :contentId AND eventType = :eventType")
    suspend fun countEventsForContent(contentId: String, eventType: EventType): Int
}

