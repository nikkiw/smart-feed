package com.core.database.content

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UpdatesMetaDao {
    @Query("SELECT * FROM updates_meta WHERE id = 1")
    suspend fun getMeta(): UpdatesMetaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMeta(meta: UpdatesMetaEntity)
}