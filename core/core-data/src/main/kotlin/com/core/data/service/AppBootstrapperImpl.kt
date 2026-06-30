package com.core.data.service

import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.AppBootstrapper
import com.core.domain.service.Recommender
import com.core.domain.usecase.sync.ContentFetchScheduleUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AppBootstrapper] that runs idempotent startup tasks.
 *
 * AppStartupCoordinator may be recreated on configuration changes. Startup stays safe because
 * the initial sync is guarded by an empty-database check and scheduling is handled idempotently.
 */
@Singleton
class AppBootstrapperImpl
    @Inject
    constructor(
        private val contentItemRepository: ContentItemRepository,
        private val syncContentUseCase: SyncContentUseCase,
        private val contentFetchScheduleUseCase: ContentFetchScheduleUseCase,
        private val recommender: Recommender,
    ) : AppBootstrapper {
        override suspend fun bootstrap() {
            supervisorScope {
                launch {
                    if (contentItemRepository.isEmpty()) {
                        syncContentUseCase()
                    }
                    recommender.updateRecommendationsForUser()
                }
                launch {
                    contentFetchScheduleUseCase.schedule()
                }
            }
        }
    }
