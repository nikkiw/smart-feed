package com.core.data.service

import com.core.di.ApplicationScope
import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.AppBootstrapper
import com.core.domain.service.Recommender
import com.core.domain.usecase.sync.ContentFetchScheduleUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AppBootstrapper] that runs startup tasks using the application scope.
 */
@Singleton
class AppBootstrapperImpl
    @Inject
    constructor(
        private val contentItemRepository: ContentItemRepository,
        private val syncContentUseCase: SyncContentUseCase,
        private val contentFetchScheduleUseCase: ContentFetchScheduleUseCase,
        private val recommender: Recommender,
        @ApplicationScope private val scope: CoroutineScope,
    ) : AppBootstrapper {
        override fun bootstrap() {
            scope.launch {
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
