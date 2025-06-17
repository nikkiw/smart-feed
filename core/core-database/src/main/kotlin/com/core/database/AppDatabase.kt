package com.core.database

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.execSQL
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.ContentDao
import com.core.database.content.ContentTagsDao
import com.core.database.content.entity.ContentEntity
import com.core.database.content.entity.ContentTag
import com.core.database.content.entity.Converter
import com.core.database.content.UpdatesMetaDao
import com.core.database.content.entity.UpdatesMetaEntity
import com.core.database.embeding.ArticleEmbeddingDao
import com.core.database.event.entity.ContentInteractionStats
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.event.entity.EventLog
import com.core.database.event.EventLogDao
import com.core.database.event.entity.EventType
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.RecommendationDao
import com.core.database.recommendation.entity.UserRecommendationEntity
import com.core.database.userprofile.UserProfileDao
import com.core.database.userprofile.UserProfileEntity
import kotlinx.coroutines.runBlocking

/**
 * The main Room database class for the application.
 *
 * This database aggregates multiple entities related to content management, user profiles,
 * event logging, recommendations, and article statistics.
 *
 * It defines DAOs for accessing and manipulating each entity type and includes
 * SQL triggers for automatic updates on certain database changes.
 *
 * @property contentDao Provides access to content-related database operations.
 * @property updatesMetaDao Provides access to metadata about updates synchronization.
 * @property contentTagsDao Provides access to content tags management.
 * @property eventLogDao Provides access to user event logging.
 * @property articleInteractionStatsDao Provides access to aggregated article interaction statistics.
 * @property articleEmbeddingDao Provides access to article embeddings for similarity and search.
 * @property userProfileDao Provides access to user profile data.
 * @property recommendationDao Provides access to content and user recommendations.
 *
 * @see [RoomDatabase] Base class for Room databases.
 * @see [TypeConverters] Specifies type converters used by the database.
 */
@androidx.room.Database(
    entities = [
        ContentEntity::class,
        ArticleAttributesEntity::class,
        ContentTag::class,
        UpdatesMetaEntity::class,
        EventLog::class,
        ContentInteractionStats::class,
        UserProfileEntity::class,
        ContentRecommendationEntity::class,
        UserRecommendationEntity::class
    ],
    version = 1
)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun contentDao(): ContentDao
    abstract fun updatesMetaDao(): UpdatesMetaDao
    abstract fun contentTagsDao(): ContentTagsDao
    abstract fun eventLogDao(): EventLogDao
    abstract fun articleInteractionStatsDao(): ContentInteractionStatsDao
    abstract fun articleEmbeddingDao(): ArticleEmbeddingDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun recommendationDao(): RecommendationDao

    companion object {

        /**
         * Creates necessary SQL triggers on the database.
         *
         * - Updates content_tags after tags field is updated or a new content row is inserted.
         * - Aggregates article interaction stats after READ events in the event_log table.
         *
         * @param db The instance of [AppDatabase] on which to create triggers.
         */
        suspend fun createTrigger(db: AppDatabase) {
            Log.d("RoomCallback", "onCreate triggered")
            db.useWriterConnection {
                // Trigger updates tags after content.tags is updated
                it.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS trg_update_tags_after_update
                    AFTER UPDATE OF tags ON content
                    BEGIN
                        DELETE FROM content_tags WHERE contentId = OLD.id;
                        INSERT INTO content_tags (contentId, tagName)
                        SELECT DISTINCT NEW.id, json_each.value
                        FROM json_each(NEW.tags);
                    END;
                    """.trimIndent()
                )

                // Trigger inserts tags after new content row inserted
                it.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS trg_update_tags_after_insert
                    AFTER INSERT ON content
                    BEGIN
                        INSERT INTO content_tags (contentId, tagName)
                        SELECT DISTINCT NEW.id, json_each.value
                        FROM json_each(NEW.tags);
                    END;
                    """.trimIndent()
                )

                // Trigger updates content interaction stats after READ event
                it.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS trg_update_content_interaction_stats
                    AFTER INSERT ON event_log
                    WHEN NEW.eventType = '${EventType.READ}'
                    BEGIN
                        INSERT INTO content_interaction_stats(contentId, readCount, avgReadingTime, avgReadPercentage)
                        VALUES (
                            NEW.contentId,
                            1,
                            COALESCE(NEW.readingTimeMillis, 0),
                            COALESCE(NEW.readPercentage, 0.0)
                        )
                        ON CONFLICT(contentId) DO UPDATE SET
                            readCount = readCount + 1,
                            avgReadingTime = ((avgReadingTime * readCount) + COALESCE(NEW.readingTimeMillis, 0)) / (readCount + 1),
                            avgReadPercentage = ((avgReadPercentage * readCount) + COALESCE(NEW.readPercentage, 0.0)) / (readCount + 1);
                    END;
                    """
                )
            }
        }

        /**
         * Creates an in-memory test instance of the database with triggers set up.
         *
         * This is useful for testing purposes and allows queries on the main thread.
         *
         * @param context The Android context.
         * @return A test instance of [AppDatabase].
         */
        fun getTestDatabase(context: Context): AppDatabase {
            val db = Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .setDriver(BundledSQLiteDriver())
                .allowMainThreadQueries()
                .build()

            runBlocking {
                createTrigger(db)
            }
            return db
        }
    }
}
