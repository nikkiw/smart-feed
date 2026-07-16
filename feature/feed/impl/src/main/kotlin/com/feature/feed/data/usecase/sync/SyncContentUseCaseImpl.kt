package com.feature.feed.data.usecase.sync

import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import javax.inject.Inject

/** Executes a user-requested sync directly; background scheduling remains owned by WorkManager. */
class SyncContentUseCaseImpl
    @Inject
    constructor(
        private val contentItemRepository: ContentItemRepository,
        private val failurePolicy: ManualSyncFailurePolicy,
    ) : SyncContentUseCase {
        override suspend fun invoke(): Result<Unit> =
            failurePolicy.failureForNextAttempt()?.let(Result.Companion::failure)
                ?: contentItemRepository.syncContent()
    }
