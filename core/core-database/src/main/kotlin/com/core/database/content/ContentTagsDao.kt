package com.core.database.content

import androidx.room.Dao
import androidx.room.Query


@Dao
interface ContentTagsDao {

    @Query("SELECT DISTINCT tagName FROM content_update_tags")
    suspend fun allTags(): List<String>
}