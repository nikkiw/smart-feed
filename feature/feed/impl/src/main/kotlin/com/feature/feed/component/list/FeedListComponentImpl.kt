package com.feature.feed.component.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.core.content.model.ContentId
import com.core.observers.ConnectivityRepository
import com.feature.feed.component.list.store.FeedIntent
import com.feature.feed.component.list.store.FeedLabel
import com.feature.feed.component.list.store.FeedState
import com.feature.feed.component.list.store.FeedStoreFactory
import com.feature.feed.data.usecase.content.GetPagedContentUseCase
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.repository.Query
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.list.FeedListComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Implementation of the Feed List Component with support for Paging and Pull-to-Refresh.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FeedListComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    getPagedContentUseCase: GetPagedContentUseCase,
    syncContentUseCase: SyncContentUseCase,
    private val connectivityRepository: ConnectivityRepository,
    contentItemRepository: ContentItemRepository,
    initialQuery: Query,
    private val onItemClick: (ContentId) -> Unit,
) : FeedListComponent, ComponentContext by componentContext {
    private val componentScope = coroutineScope()

    private val _model =
        MutableValue(
            FeedListComponent.Model(
                isOnline = connectivityRepository.isConnected.value,
                hasLocalContent = false,
            ),
        )
    override val model: Value<FeedListComponent.Model> = _model

    private val effectChannel = Channel<FeedListComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<FeedListComponent.Effect> = effectChannel.receiveAsFlow()

    private val feedStore =
        instanceKeeper.getStore {
            FeedStoreFactory(
                storeFactory = storeFactory,
                initialQuery = initialQuery,
                syncContentUseCase = syncContentUseCase,
                connectivityRepository = connectivityRepository,
                contentItemRepository = contentItemRepository,
            ).create()
        }

    override val pagingItems: Flow<PagingData<ContentItemPreview>> =
        feedStore.states
            .map { it.query }
            .distinctUntilChanged()
            .flatMapLatest(getPagedContentUseCase::invoke)
            .cachedIn(componentScope)

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            feedStore.states bindTo ::renderState
            feedStore.labels bindTo ::handleLabel
        }
    }

    private fun renderState(state: FeedState) {
        _model.value =
            FeedListComponent.Model(
                isOnline = state.isOnline,
                hasLocalContent = state.hasLocalContent,
                refreshState =
                    when (val refreshState = state.refreshState) {
                        FeedState.RefreshState.Idle -> FeedListComponent.RefreshState.Idle
                        is FeedState.RefreshState.Refreshing -> FeedListComponent.RefreshState.Refreshing
                        is FeedState.RefreshState.Failed ->
                            FeedListComponent.RefreshState.Failed(refreshState.message)
                    },
            )
    }

    private fun handleLabel(label: FeedLabel) {
        when (label) {
            is FeedLabel.OpenArticle -> onItemClick(label.contentId)
            FeedLabel.OpenInternetSettings ->
                effectChannel.trySend(FeedListComponent.Effect.OpenInternetSettings)
        }
    }

    override fun onRefresh() {
        feedStore.accept(FeedIntent.RefreshRequested)
    }

    override fun onRetry() {
        feedStore.accept(FeedIntent.RetryRequested)
    }

    override fun onEnableInternetClicked() {
        feedStore.accept(FeedIntent.EnableInternetClicked)
    }

    override fun onListItemClick(itemId: ContentId) {
        feedStore.accept(FeedIntent.ArticleClicked(itemId))
    }

    override fun updateQuery(query: Query) {
        feedStore.accept(FeedIntent.QueryChanged(query))
    }
}
