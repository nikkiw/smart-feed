package com.feature.feed.domain.usecase.sync

/**
 * Use case interface for synchronizing content from a remote source (e.g., API) to local storage.
 *
 * This interface abstracts fetching and persisting updates and rebuilding local recommendation
 * projections, typically used when manually triggering sync or during app startup.
 *
 * ### Example usage:
 * ```kotlin
 * val result = syncContentUseCase()
 * if (result.isSuccess) {
 *     showSuccessMessage()
 * } else {
 *     showErrorMessage(result.exceptionOrNull())
 * }
 * ```
 */
interface SyncContentUseCase {
    /**
     * Synchronizes content and rebuilds derived recommendations in the local data store.
     *
     * @return A [Result] indicating success or failure of the synchronization operation.
     */
    suspend operator fun invoke(): Result<Unit>
}
