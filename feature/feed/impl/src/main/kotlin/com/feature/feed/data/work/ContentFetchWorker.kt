package com.feature.feed.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ContentFetchWorker
@AssistedInject
constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncContentUseCase: SyncContentUseCase,
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val KEY_ERROR_MESSAGE = "key_error_message"
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun doWork(): Result = try {
        syncContentUseCase().getOrThrow()
        Result.success()
    } catch (e: Exception) {
        val data = workDataOf(KEY_ERROR_MESSAGE to (e.message ?: "Unknown error"))
        Result.failure(data)
    }
}
