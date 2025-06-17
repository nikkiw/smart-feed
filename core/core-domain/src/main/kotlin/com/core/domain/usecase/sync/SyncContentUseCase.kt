package com.core.domain.usecase.sync

/**
 * Use case interface for synchronizing content from a remote source (e.g., API) to local storage.
 *
 * This interface abstracts the process of fetching and persisting updates in the background,
 * typically used when manually triggering sync or during app startup.
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
     * Synchronizes content from a remote source into the app's local data store.
     *
     * @return A [Result] indicating success or failure of the synchronization operation.
     */
    suspend operator fun invoke(): Result<Unit>
}