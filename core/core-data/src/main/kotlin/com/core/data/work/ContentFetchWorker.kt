package com.core.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.Recommender
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ContentFetchWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val contentItemRepository: ContentItemRepository,
    private val recommender: Recommender
) : CoroutineWorker(appContext, workerParams) {
    companion object {
        const val KEY_ERROR_MESSAGE = "key_error_message"
    }

    override suspend fun doWork(): Result = try {
        contentItemRepository.syncContent()
            .getOrThrow()
        recommender.updateRecommendationsForUser()
        recommender.updateRecommendationsForArticles()
        Result.success()
    } catch (e: Exception) {
        val data = workDataOf(KEY_ERROR_MESSAGE to (e.message ?: "Unknown error"))
        Result.failure(data)
    }
}
