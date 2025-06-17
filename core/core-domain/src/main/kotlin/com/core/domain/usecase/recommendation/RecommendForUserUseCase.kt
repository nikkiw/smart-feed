package com.core.domain.usecase.recommendation

import com.core.domain.model.ContentItemPreview
import kotlinx.coroutines.flow.Flow


/**
 * Use case interface for retrieving personalized content recommendations for the current user.
 *
 * This interface provides a way to fetch a stream of recommended content items based on user preferences,
 * behavior, or profile. It is typically used in the UI layer to display a dynamic list of suggestions.
 *
 * ### Example usage:
 * ```kotlin
 * recommendForUserUseCase().launchAndCollectIn(viewModelScope) {
 *     adapter.submitList(it)
 * }
 * ```
 */
interface RecommendForUserUseCase {

    /**
     * Retrieves a [Flow] of recommended content items for the current user.
     *
     * The list contains preview versions of content items suitable for displaying in the UI.
     *
     * @return A [Flow] emitting lists of [ContentItemPreview] objects.
     */
    operator fun invoke(): Flow<List<ContentItemPreview>>
}


