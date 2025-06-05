package com.core.database.content

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "updates_meta")
data class UpdatesMetaEntity(
    @PrimaryKey val id: Int = 1,
    val lastSyncAt: String
)
