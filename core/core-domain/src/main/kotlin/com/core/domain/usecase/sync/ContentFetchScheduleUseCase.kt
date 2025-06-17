package com.core.domain.usecase.sync

/**
 * Use case interface for scheduling background content fetching.
 *
 * This interface provides a way to trigger periodic  content updates,
 * such as syncing new articles from a remote source in the background.
 *
 * ### Example usage:
 * ```kotlin
 * contentFetchScheduleUseCase.schedule()
 * ```
 */
interface ContentFetchScheduleUseCase {

    /**
     * Schedules background content fetching or synchronization.
     *
     * This may include setting up periodic syncs, triggering one-time jobs,
     * or rescheduling after app restart, depending on implementation.
     */
    fun schedule()
}