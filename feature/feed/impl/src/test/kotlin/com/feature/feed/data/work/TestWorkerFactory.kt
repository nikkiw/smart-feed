package com.feature.feed.data.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.feature.feed.domain.usecase.sync.SyncContentUseCase

class TestWorkerFactory(
    private val syncContentUseCase: SyncContentUseCase,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            ContentFetchWorker::class.java.name ->
                ContentFetchWorker(appContext, workerParameters, syncContentUseCase)

            else -> null
        }
    }
}
