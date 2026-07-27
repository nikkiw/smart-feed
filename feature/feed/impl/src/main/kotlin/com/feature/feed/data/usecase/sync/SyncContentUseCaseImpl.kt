package com.feature.feed.data.usecase.sync

import com.core.common.coroutines.runSuspendCatching
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.recommendation.domain.service.Recommender
import javax.inject.Inject

/** Synchronizes remote content and rebuilds the local recommendation projections. */
class SyncContentUseCaseImpl
@Inject
constructor(
    private val contentItemRepository: ContentItemRepository,
    private val failurePolicy: ManualSyncFailurePolicy,
    private val recommender: Recommender,
) : SyncContentUseCase {
    override suspend fun invoke(): Result<Unit> = failurePolicy.failureForNextAttempt()?.let(Result.Companion::failure)
        ?: syncContentAndRecommendations()

    private suspend fun syncContentAndRecommendations(): Result<Unit> {
        val syncResult = contentItemRepository.syncContent()
        if (syncResult.isFailure) return syncResult

        return runSuspendCatching {
            recommender.updateRecommendationsForUser()
            recommender.updateRecommendationsForArticles()
        }
    }
}
