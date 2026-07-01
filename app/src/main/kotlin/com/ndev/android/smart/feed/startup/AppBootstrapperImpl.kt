package com.ndev.android.smart.feed.startup

import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.sync.ContentFetchScheduleUseCase
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.recommendation.domain.service.Recommender
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-level implementation of [AppBootstrapper] that runs idempotent startup tasks.
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
