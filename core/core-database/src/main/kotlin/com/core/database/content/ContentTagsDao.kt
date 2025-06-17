package com.core.database.content

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


/**
 * Data Access Object (DAO) for accessing content tag data.
 *
 * Provides reactive access to the list of all distinct tags used in content entries.
 */
@Dao
interface ContentTagsDao {

    /**
     * Returns a stream of all unique tag names found in the `content_tags` table.
     *
     * The result is reactive and will emit updates whenever the underlying table changes.
     *
     * @return A [Flow] emitting the list of distinct tag names.
     */
    @Query("SELECT DISTINCT tagName FROM content_tags")
    fun allTags(): Flow<List<String>>
}
