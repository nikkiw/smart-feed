package com.core.domain.usecase.content

import com.core.domain.model.ContentId
import com.core.domain.model.ContentItem


/**
 * Use case interface for retrieving a specific content item by its ID.
 *
 * This interface abstracts the source of the content (e.g., local repository, remote API,
 * or combined data source) and provides a clean way to fetch content in the application layer.
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
 */
interface GetContentItemUseCase {

    /**
     * Retrieves the content item with the given ID.
     *
     * @param itemId The unique identifier of the content item to retrieve.
     * @return A [Result] containing the requested [ContentItem], or an error if retrieval failed.
     */
    suspend operator fun invoke(itemId: ContentId): Result<ContentItem>
}
