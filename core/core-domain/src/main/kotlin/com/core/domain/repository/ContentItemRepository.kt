package com.core.domain.repository


import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview
import com.core.domain.model.ContentType
import com.core.domain.model.Tags
import kotlinx.coroutines.flow.Flow

/**
 * Represents available sorting options for content items.
 */
enum class ContentItemsSortedType {
    /**
     * Sort content items by name in ascending order.
     */
    ByNameAsc,

    /**
     * Sort content items by name in descending order.
     */
    ByNameDesc,

    /**
     * Sort content items by date, newest first.
     */
    ByDateNewestFirst,

    /**
     * Sort content items by date, oldest first.
     */
    ByDateOldestFirst
}

/**
 * Represents a query object used to filter and sort content items.
 *
 * @property types List of [ContentType] to include in the result.
 * @property tags Tags used as a filter — only content items with matching tags will be included.
 * @property sortedBy Specifies how the results should be sorted, using [ContentItemsSortedType].
 */
data class Query(
    val types: List<ContentType>,
    val tags: Tags,
    val sortedBy: ContentItemsSortedType
)

/**
 * Repository interface responsible for fetching and persisting content updates.
 */
interface ContentItemRepository {

    /**
     * Returns a Flow of paginated content items based on the provided query.
     *
     * This method is typically used for UI components that need to display
     * large sets of data efficiently using Paging.
     *
     * @param query Filtering and sorting criteria for the content.
     * @return A Flow of [PagingData] containing [ContentItemPreview] objects.
     */
    fun flowContent(query: Query): Flow<PagingData<ContentItemPreview>>

    /**
     * Fetches a single content item by its ID.
     *
     * @param itemId Unique identifier of the content item.
     * @return A [Result] containing the requested [ContentItem], or an error if the operation fails.
     */
    suspend fun getContentById(itemId: ContentId): Result<ContentItem>

    /**
     * Checks whether the repository currently holds no data.
     *
     * @return `true` if the repository is empty, `false` otherwise.
     */
    suspend fun isEmpty(): Boolean

    /**
     * Returns a Flow of all available tags from the repository.
     *
     * Useful for displaying tag filters in the UI.
     *
     * @return A Flow of [Tags] object containing a list of strings.
     */
    fun flowAllTags(): Flow<Tags>

    /**
     * Synchronizes content with a remote source (e.g., API), then persists changes locally.
     *
     * @return A [Result] indicating success or failure of the sync operation.
     */
    suspend fun syncContent(): Result<Unit>
}