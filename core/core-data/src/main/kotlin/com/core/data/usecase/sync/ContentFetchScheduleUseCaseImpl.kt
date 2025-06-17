package com.core.data.usecase.sync

import com.core.data.work.ContentFetchScheduler
import com.core.domain.usecase.sync.ContentFetchScheduleUseCase
import javax.inject.Inject

/**
 * Default implementation of [ContentFetchScheduleUseCase].
 *
 * Delegates the scheduling logic to [ContentFetchScheduler], which uses WorkManager
 * to schedule periodic content fetching jobs in the background.
 *
 * ### Example usage:
 * ```kotlin
 * contentFetchScheduleUseCase.schedule()
 * ```
 *
 * @param contentFetchScheduler Scheduler responsible for setting up background work.
 * @see ContentFetchScheduleUseCase for interface definition
 */
class ContentFetchScheduleUseCaseImpl @Inject constructor(
    private val contentFetchScheduler: ContentFetchScheduler
) : ContentFetchScheduleUseCase {

    /**
     * Schedules background content fetching by delegating to [ContentFetchScheduler].
     */
    override fun schedule() {
        contentFetchScheduler.schedule()
    }
}