package com.core.database.content

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction

@Dao
interface ContentDao {

    @Transaction
    @RawQuery(
        observedEntities = [
            ContentUpdateEntity::class,
            ArticleAttributesEntity::class
        ]
    )
    fun getContent(query: RoomRawQuery): PagingSource<Int, ContentUpdateWithDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentUpdate(contentUpdate: ContentUpdateEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticleAttributes(article: ArticleAttributesEntity)

    @Transaction
    suspend fun insertContentUpdateWithDetails(
        contentUpdate: ContentUpdateEntity,
        article: ArticleAttributesEntity? = null
    ) {
        insertContentUpdate(contentUpdate)
        article?.let { insertArticleAttributes(it) }
    }

    @Delete
    suspend fun deleteContentUpdate(contentUpdate: ContentUpdateEntity)

    @Query("DELETE FROM content_updates WHERE id = :id")
    suspend fun deleteContentUpdateById(id: String)
}