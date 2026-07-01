package com.feature.feed.data.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.recommendation.domain.service.Recommender

class TestWorkerFactory(
    private val repository: ContentItemRepository,
    private val recommender: Recommender,
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        return when (workerClassName) {
            ContentFetchWorker::class.java.name ->
                ContentFetchWorker(appContext, workerParameters, repository, recommender)

            else -> null
        }
    }
}
