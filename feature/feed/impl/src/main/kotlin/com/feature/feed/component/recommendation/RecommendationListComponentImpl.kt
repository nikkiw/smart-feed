package com.feature.feed.component.recommendation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.core.content.model.ContentId
import com.core.observers.ConnectivityRepository
import com.feature.feed.component.recommendation.store.RecommendationStore
import com.feature.feed.component.recommendation.store.RecommendationStoreFactory
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.recommendation.RecommendationListComponent
import com.feature.recommendation.domain.usecase.RecommendForUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

class RecommendationListComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    recommendForUserUseCase: RecommendForUserUseCase,
    connectivityRepository: ConnectivityRepository,
    contentItemRepository: ContentItemRepository,
    syncContentUseCase: SyncContentUseCase,
    private val onItemClick: (ContentId) -> Unit,
) : RecommendationListComponent, ComponentContext by componentContext {
    private val store =
        instanceKeeper.getStore {
            RecommendationStoreFactory(
                storeFactory = storeFactory,
                recommendForUserUseCase = recommendForUserUseCase,
                connectivityRepository = connectivityRepository,
                contentItemRepository = contentItemRepository,
                syncContentUseCase = syncContentUseCase,
            ).create()
        }

    private val _model =
        MutableValue(
            RecommendationListComponent.Model(
                isOnline = connectivityRepository.isConnected.value,
            ),
        )
    override val model: Value<RecommendationListComponent.Model> = _model

    private val effectChannel = Channel<RecommendationListComponent.Effect>(Channel.BUFFERED)
    override val effects: Flow<RecommendationListComponent.Effect> = effectChannel.receiveAsFlow()

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.states bindTo ::render
            store.labels bindTo ::handleLabel
        }
    }

    override fun onRetry() = store.accept(RecommendationStore.Intent.Retry)

    override fun onEnableInternetClicked() = store.accept(RecommendationStore.Intent.EnableInternetClicked)

    override fun onListItemClick(itemId: ContentId) = store.accept(RecommendationStore.Intent.ArticleClicked(itemId))

    private fun render(state: RecommendationStore.State) {
        _model.value =
            RecommendationListComponent.Model(
                items = state.items,
                isOnline = state.isOnline,
                hasLocalContent = state.hasLocalContent,
                loadState =
                    when (val loadState = state.loadState) {
                        RecommendationStore.LoadState.Loading -> RecommendationListComponent.LoadState.Loading
                        RecommendationStore.LoadState.Idle -> RecommendationListComponent.LoadState.Idle
                        is RecommendationStore.LoadState.Failed ->
                            RecommendationListComponent.LoadState.Failed(loadState.message)
                    },
            )
    }

    private fun handleLabel(label: RecommendationStore.Label) {
        when (label) {
            is RecommendationStore.Label.OpenArticle -> onItemClick(label.id)
            RecommendationStore.Label.OpenInternetSettings ->
                effectChannel.trySend(RecommendationListComponent.Effect.OpenInternetSettings)
        }
    }
}
