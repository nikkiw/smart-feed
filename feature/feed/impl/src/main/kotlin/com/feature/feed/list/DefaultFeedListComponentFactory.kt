package com.feature.feed.list

import com.arkivanov.decompose.ComponentContext
import com.core.domain.model.ContentId
import com.core.domain.repository.Query
import com.core.domain.usecase.sync.SyncContentUseCase
import com.core.observers.ConnectivityRepository
import com.core.paging.GetPagedContentUseCase
import javax.inject.Inject

class DefaultFeedListComponentFactory
    @Inject
    constructor(
        private val getPagedContentUseCase: GetPagedContentUseCase,
        private val syncContentUseCase: SyncContentUseCase,
        private val connectivityRepository: ConnectivityRepository,
    ) : FeedListComponent.Factory {
        override fun invoke(
            componentContext: ComponentContext,
            initialQuery: Query,
            onItemClick: (ContentId) -> Unit,
        ): FeedListComponent =
            FeedListComponentImpl(
                componentContext = componentContext,
                getPagedContentUseCase = getPagedContentUseCase,
                syncContentUseCase = syncContentUseCase,
                connectivityRepository = connectivityRepository,
                initialQuery = initialQuery,
                onItemClick = onItemClick,
            )
    }
