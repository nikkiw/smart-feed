package com.core.database.content

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.execSQL
import androidx.room.useWriterConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.runBlocking

@Database(
    entities = [
        ContentUpdateEntity::class,
        ArticleAttributesEntity::class,
        ContentUpdateTagCrossRef::class,
        UpdatesMetaEntity::class],
    version = 1
)
@TypeConverters(TagsConverter::class)
abstract class ContentDatabase : RoomDatabase() {
    abstract fun contentDao(): ContentDao
    abstract fun updatesMetaDao(): UpdatesMetaDao
    abstract fun contentTagsDao(): ContentTagsDao

    companion object {

        suspend fun createTrigger(db: ContentDatabase) {

            Log.d("RoomCallback", "onCreate triggered")
            // создаём триггер сразу после того, как таблицы будут созданы
            db.useWriterConnection{
                it.execSQL(
                    """
                CREATE TRIGGER IF NOT EXISTS trg_update_tags_after_update
                AFTER UPDATE OF tags ON content_updates
                BEGIN
                    DELETE FROM content_update_tags WHERE contentUpdateId = OLD.id;
                    INSERT INTO content_update_tags (contentUpdateId, tagName)
                    SELECT DISTINCT NEW.id, json_each.value
                    FROM json_each(NEW.tags);
                END;
            """.trimIndent()
                )

                it.execSQL(
                    """
                    CREATE TRIGGER IF NOT EXISTS trg_update_tags_after_insert
                    AFTER INSERT ON content_updates
                    BEGIN
                        INSERT INTO content_update_tags (contentUpdateId, tagName)
                        SELECT DISTINCT NEW.id, json_each.value
                        FROM json_each(NEW.tags);
                    END;
                """.trimIndent()
                )
            }
        }

        fun getTestDatabase(context: Context): ContentDatabase{
            val db = Room
                .inMemoryDatabaseBuilder(context, ContentDatabase::class.java)
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
