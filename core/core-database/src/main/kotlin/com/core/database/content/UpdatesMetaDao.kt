package com.core.database.content

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.core.database.content.entity.UpdatesMetaEntity

/**
 * DAO for managing update metadata.
 *
 * Provides methods to retrieve and save information about the last synchronization.
 */
@Dao
interface UpdatesMetaDao {

    /**
     * Retrieves the current update metadata.
     * Usually contains the timestamp of the last successful sync.
     *
     * @return [UpdatesMetaEntity] object or null if metadata is not yet saved.
     */
    @Query("SELECT * FROM updates_meta WHERE id = 1")
    suspend fun getMeta(): UpdatesMetaEntity?

    /**
     * Saves the update metadata.
     * On conflict, replaces the existing entry with id = 1.
     *
     * @param meta The metadata entity to save.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMeta(meta: UpdatesMetaEntity)


    /**
     * Retrieves all records from the updates_meta table.
     * This method is useful for testing and verification purposes,
     * to ensure that only one metadata record with id = 1 exists in the table.
     *
     * @return A list of all [UpdatesMetaEntity] entries currently stored in the database.
     */
    @Query("SELECT * FROM updates_meta")
    suspend fun getAll(): List<UpdatesMetaEntity>
}
