package com.core.data.usecase.sync

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import com.core.data.work.ContentFetchWorker
import com.core.domain.usecase.sync.SyncContentUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of [SyncContentUseCase] that uses WorkManager to perform a one-time content sync.
 *
 * This class schedules a manual sync job using [ContentFetchWorker], waits for its completion,
 * and returns a [Result] indicating success or failure based on the final state of the work.
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
 *
 * @param workManager Android WorkManager instance used to enqueue and monitor background work.
 * @param uniqueWorkName Unique name for the sync job to avoid duplication.
 * @param tagWork Tag used to identify this sync job in logs or debugging tools.
 */
class SyncContentUseCaseImpl @Inject constructor(
    private val workManager: WorkManager,
    @Named("unique_work_name") private val uniqueWorkName: String,
    @Named("tag_work") private val tagWork: String
) : SyncContentUseCase {

    /**
     * Synchronizes content from a remote source by enqueuing a one-time background sync job.
     *
     * 1. Creates a one-time work request with network constraints.
     * 2. Enqueues it as a unique job so only one sync runs at a time.
     * 3. Waits for the job to finish.
     * 4. Returns a [Result] based on the final state of the job.
     *
     * If the job succeeds, returns [Result.success].
     * If the job fails or is cancelled, returns [Result.failure] with an error message
     * extracted from the worker's output data (or a default message).
     *
     * @return A [Result] indicating success or failure of the synchronization operation.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(): Result<Unit> {
        val request = OneTimeWorkRequestBuilder<ContentFetchWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .addTag(tagWork)
            .build()

        // Enqueue the work
        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            request
        ).await()

        // Wait until the work finishes
        return try {
            val info = workManager.getWorkInfosForUniqueWorkFlow(uniqueWorkName)
                .first { workInfos ->
                    workInfos.isNotEmpty() && workInfos.first().state.isFinished
                }
                .first()

            when (info.state) {
                WorkInfo.State.SUCCEEDED -> Result.success(Unit)
                WorkInfo.State.FAILED,
                WorkInfo.State.CANCELLED -> {
                    val errorMessage =
                        info.outputData.getString(ContentFetchWorker.KEY_ERROR_MESSAGE)
                            ?: "Sync failed with state ${info.state}"
                    Result.failure(RuntimeException(errorMessage))
                }

                else -> Result.failure(RuntimeException("Unexpected final state: ${info.state}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
