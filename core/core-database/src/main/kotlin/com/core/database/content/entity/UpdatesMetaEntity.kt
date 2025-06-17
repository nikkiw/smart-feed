package com.core.database.content.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity that stores metadata about content updates, such as the timestamp
 * of the last successful synchronization.
 *
 * This table is intended to contain a single row, identified by a fixed primary key [id] = 1.
 *
 * @property id Primary key for the metadata row. Always set to 1.
 * @property lastSyncAt ISO 8601 timestamp of the last successful sync operation.
 */
@Entity(tableName = "updates_meta")
data class UpdatesMetaEntity(
    @PrimaryKey val id: Int = 1,
    val lastSyncAt: String
)