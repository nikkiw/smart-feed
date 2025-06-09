package com.core.database.content

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface ContentTagsDao {

    @Query("SELECT DISTINCT tagName FROM content_update_tags")
    fun allTags(): Flow<List<String>>
}