package com.core.data.usecase.content

import androidx.paging.PagingData
import com.core.domain.model.ContentItemPreview
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.Query
import com.core.domain.usecase.content.GetContentUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


/**
 * Default implementation of [GetContentUseCase].
 *
 * Retrieves a flow of paginated content items using the provided [ContentItemRepository].
 * This class applies filtering and sorting criteria defined in the [Query] object,
 * making it suitable for displaying content in a paged UI (e.g., RecyclerView with Paging).
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
 *
 * @param contentItemRepository Repository used to fetch content from local or remote sources.
 * @see GetContentUseCase for interface definition
 * @see ContentItemRepository for data source details
 */
class GetContentUseCaseImpl @Inject constructor(
    private val contentItemRepository: ContentItemRepository
) : GetContentUseCase {

    /**
     * Retrieves a flow of paginated content previews based on the provided query.
     *
     * @param query Filtering and sorting criteria for the content.
     * @return A [Flow] of [PagingData] containing [ContentItemPreview] objects.
     */
    override operator fun invoke(query: Query): Flow<PagingData<ContentItemPreview>> {
        return contentItemRepository.flowContent(query)
    }
}