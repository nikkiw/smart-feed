package com.core.domain.usecase.content

import androidx.paging.PagingData
import com.core.domain.model.ContentItemPreview
import com.core.domain.repository.Query
import kotlinx.coroutines.flow.Flow


/**
 * Use case interface for retrieving a paginated list of content items based on a query.
 *
 * This interface abstracts the data source and provides a unified way to fetch content
 * with filtering and sorting options, suitable for UI components like RecyclerView with Paging.
 *
 * ### Example usage:
 * ```kotlin
 * val query = Query(
 *     types = listOf(ContentType.ARTICLE),
 *     tags = Tags(listOf("tech", "ai")),
 *     sortedBy = ContentItemsSortedType.ByDateNewestFirst
 * )
 *
 * getContentUseCase(query).launchAndCollectIn(viewModelScope) {
 *     adapter.submitData(it)
 * }
 * ```
 */
interface GetContentUseCase {

    /**
     * Retrieves a flow of paginated content items that match the given [Query].
     *
     * @param query Filtering and sorting criteria for the content.
     * @return A [Flow] of [PagingData] containing [ContentItemPreview] objects.
     */
    operator fun invoke(query: Query): Flow<PagingData<ContentItemPreview>>
}
