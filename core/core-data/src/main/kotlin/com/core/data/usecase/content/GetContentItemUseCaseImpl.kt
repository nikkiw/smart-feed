package com.core.data.usecase.content

import com.core.domain.model.ContentId
import com.core.domain.model.ContentItem
import com.core.domain.repository.ContentItemRepository
import com.core.domain.usecase.content.GetContentItemUseCase
import javax.inject.Inject


/**
 * Default implementation of [GetContentItemUseCase].
 *
 * This class retrieves content items by ID using the provided [ContentItemRepository],
 * which may fetch data from local or remote sources.
 *
 * ### Example usage:
 * ```kotlin
 * val result = getContentItemUseCase(contentId)
 * if (result.isSuccess) {
 *     showContent(result.getOrThrow())
 * } else {
 *     showError(result.exceptionOrNull())
 * }
 * ```
 *
 * @param contentItemRepository Repository used to fetch content items.
 * @see GetContentItemUseCase for interface definition
 * @see ContentItemRepository for data source details
 */
class GetContentItemUseCaseImpl @Inject constructor(
    private val contentItemRepository: ContentItemRepository
) : GetContentItemUseCase {

    /**
     * Retrieves the content item with the given ID using the underlying repository.
     *
     * @param itemId The unique identifier of the content item to retrieve.
     * @return A [Result] containing the requested [ContentItem], or an error if retrieval failed.
     */
    override suspend fun invoke(itemId: ContentId): Result<ContentItem> {
        return contentItemRepository.getContentById(itemId)
    }
}