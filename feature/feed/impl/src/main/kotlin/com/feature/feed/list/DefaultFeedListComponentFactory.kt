package com.feature.feed.list

import com.arkivanov.decompose.ComponentContext
import com.core.content.model.ContentId
import com.core.observers.ConnectivityRepository
import com.feature.feed.data.usecase.content.GetPagedContentUseCase
import com.feature.feed.domain.repository.Query
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
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
