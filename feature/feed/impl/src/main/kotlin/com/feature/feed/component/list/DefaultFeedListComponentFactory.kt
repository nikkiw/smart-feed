package com.feature.feed.component.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.core.content.model.ContentId
import com.core.observers.ConnectivityRepository
import com.feature.feed.data.usecase.content.GetPagedContentUseCase
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.repository.Query
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.list.FeedListComponent
import javax.inject.Inject

class DefaultFeedListComponentFactory
    @Inject
    constructor(
        private val storeFactory: StoreFactory,
        private val getPagedContentUseCase: GetPagedContentUseCase,
        private val syncContentUseCase: SyncContentUseCase,
        private val connectivityRepository: ConnectivityRepository,
        private val contentItemRepository: ContentItemRepository,
    ) : FeedListComponent.Factory {
        override fun invoke(
            componentContext: ComponentContext,
            initialQuery: Query,
            onItemClick: (ContentId) -> Unit,
        ): FeedListComponent =
            FeedListComponentImpl(
                componentContext = componentContext,
                storeFactory = storeFactory,
                getPagedContentUseCase = getPagedContentUseCase,
                syncContentUseCase = syncContentUseCase,
                connectivityRepository = connectivityRepository,
                contentItemRepository = contentItemRepository,
                initialQuery = initialQuery,
                onItemClick = onItemClick,
            )
    }
