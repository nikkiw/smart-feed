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
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.entity.ContentEntity
import com.core.database.content.entity.ContentPreviewWithDetails
import com.core.database.content.entity.ContentWithDetails

/**
 * Data Access Object (DAO) for accessing and manipulating content data,
 * including main content records, related article attributes, and associated previews.
 *
 * This interface defines database operations for paginated and transactional access
 * to content entities and their relational structure using Room.
 */
@Dao
interface ContentDao {

    /**
     * Returns a [PagingSource] for paginated access to content previews with article details,
     * using a raw SQL query constructed externally.
     *
     * This method supports complex, dynamic queries via [RoomRawQuery],
     * and ensures proper observation of content and article entities for Flow updates.
     *
     * @param query A raw SQL query with bound arguments for filtering and sorting.
     * @return A [PagingSource] emitting [ContentPreviewWithDetails] entries.
     */
    @Transaction
    @RawQuery(
        observedEntities = [
            ContentEntity::class,
            ArticleAttributesEntity::class
        ]
    )
    fun getContent(query: RoomRawQuery): PagingSource<Int, ContentPreviewWithDetails>

    /**
     * Retrieves the most recently updated content entries up to a specified [limit],
     * ordered by descending timestamp.
     *
     * @param limit Maximum number of results to return.
     * @return A list of [ContentPreviewWithDetails] sorted by most recent update first.
     */
    @Transaction
    @Query("SELECT * FROM content ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun getRecentContent(limit: Int): List<ContentPreviewWithDetails>

    /**
     * Checks whether the `content` table contains any entries.
     *
     * @return `true` if there is at least one content record, otherwise `false`.
     */
    @Query("SELECT COUNT(*) > 0 FROM content")
    suspend fun isNotEmpty(): Boolean

    /**
     * Retrieves a single content entry along with its associated article details
     * by its unique [id].
     *
     * @param id The content identifier.
     * @return A [ContentWithDetails] record if found.
     */
    @Transaction
    @Query("SELECT * FROM content WHERE id = :id")
    suspend fun getContentById(id: String): ContentWithDetails

    /**
     * Inserts or updates a single [ContentEntity] in the database.
     * If an entry with the same ID exists, it is replaced.
     *
     * @param contentUpdate The content entity to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentUpdate(contentUpdate: ContentEntity)

    /**
     * Inserts or updates an [ArticleAttributesEntity] in the database.
     * If an entry with the same ID exists, it is replaced.
     *
     * @param article The article attributes entity to insert or update.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticleAttributes(article: ArticleAttributesEntity)

    /**
     * Transactionally inserts or updates a [ContentEntity] and its associated
     * [ArticleAttributesEntity], if provided.
     *
     * Ensures both entities are updated atomically in a single transaction.
     *
     * @param contentUpdate The main content entity.
     * @param article Optional related article attributes.
     */
    @Transaction
    suspend fun insertContentUpdateWithDetails(
        contentUpdate: ContentEntity,
        article: ArticleAttributesEntity? = null
    ) {
        insertContentUpdate(contentUpdate)
        article?.let { insertArticleAttributes(it) }
    }

    /**
     * Deletes a specific [ContentEntity] from the database.
     *
     * @param contentUpdate The content entity to delete.
     */
    @Delete
    suspend fun deleteContentUpdate(contentUpdate: ContentEntity)

    /**
     * Deletes a [ContentEntity] by its unique [id].
     *
     * @param id The ID of the content to delete.
     */
    @Query("DELETE FROM content WHERE id = :id")
    suspend fun deleteContentUpdateById(id: String)
}
