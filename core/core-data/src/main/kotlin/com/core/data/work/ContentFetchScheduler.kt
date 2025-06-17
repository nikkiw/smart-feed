package com.core.data.work

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Scheduler responsible for setting up periodic background work to fetch  content.
 *
 * Uses WorkManager to ensure reliable execution even if the app is not in the foreground.
 * The interval and flex time are configured via [WorkerScheduleConfig].
 *
 * @param workManager Android WorkManager instance used to enqueue background work.
 * @param config Configuration object defining fetch interval and flex time.
 */
@Singleton
class ContentFetchScheduler @Inject constructor(
    private val workManager: WorkManager,
    private val config: WorkerScheduleConfig,
    @Named("workName") private val workName: String
) {

    /**
     * Schedules a periodic background job to fetch new content.
     *
     * - Requires network connectivity.
     * - Uses [ContentFetchWorker] as the worker class.
     * - Enqueues the work as a unique periodic job with policy [ExistingPeriodicWorkPolicy.KEEP].
     */
    fun schedule() {
        val work = PeriodicWorkRequestBuilder<ContentFetchWorker>(
            config.fetchInterval.toMillis(),
            TimeUnit.MILLISECONDS,
            config.fetchFlex.toMillis(),
            TimeUnit.MILLISECONDS
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager
            .enqueueUniquePeriodicWork(
                workName,
                ExistingPeriodicWorkPolicy.KEEP,
                work
            )
    }
}