package com.feature.feed.component.recommendation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.core.observers.ConnectivityRepository
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.recommendation.domain.usecase.RecommendForUserUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class RecommendationStoreFactory(
    private val storeFactory: StoreFactory,
    private val recommendForUserUseCase: RecommendForUserUseCase,
    private val connectivityRepository: ConnectivityRepository,
    private val contentItemRepository: ContentItemRepository,
    private val syncContentUseCase: SyncContentUseCase,
) {
    fun create(): RecommendationStore =
        object :
            RecommendationStore,
            Store<RecommendationStore.Intent, RecommendationStore.State, RecommendationStore.Label> by
            storeFactory.create(
                name = "RecommendationStore",
                initialState = RecommendationStore.State(isOnline = connectivityRepository.isConnected.value),
                bootstrapper = SimpleBootstrapper(Action.Bootstrap),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Action {
        data object Bootstrap : Action
    }

    private sealed interface Msg {
        data object Loading : Msg

        data class ItemsLoaded(val items: List<ContentItemPreview>) : Msg

        data class Failed(val message: String) : Msg

        data class ConnectivityChanged(val value: Boolean) : Msg

        data class LocalAvailabilityChanged(val value: Boolean) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<
            RecommendationStore.Intent,
            Action,
            RecommendationStore.State,
            Msg,
            RecommendationStore.Label,
            >() {
        private var loadJob: Job? = null
        private var retryOnReconnect = false
        private var wasOnline = connectivityRepository.isConnected.value

        override fun executeAction(action: Action) {
            scope.launch {
                connectivityRepository.isConnected.collect { online ->
                    dispatch(Msg.ConnectivityChanged(online))
                    if (!wasOnline && online && retryOnReconnect) {
                        retryOnReconnect = false
                        syncContentUseCase()
                        startLoad()
                    }
                    wasOnline = online
                }
            }
            scope.launch {
                contentItemRepository.observeHasContent().collect {
                    dispatch(Msg.LocalAvailabilityChanged(it))
                }
            }
            startLoad()
        }

        override fun executeIntent(intent: RecommendationStore.Intent) {
            when (intent) {
                RecommendationStore.Intent.Retry -> startLoad()
                RecommendationStore.Intent.EnableInternetClicked -> {
                    retryOnReconnect = true
                    publish(RecommendationStore.Label.OpenInternetSettings)
                }
                is RecommendationStore.Intent.ArticleClicked ->
                    publish(RecommendationStore.Label.OpenArticle(intent.id))
            }
        }

        private fun startLoad() {
            loadJob?.cancel()
            dispatch(Msg.Loading)
            loadJob =
                scope.launch {
                    recommendForUserUseCase()
                        .catch { error ->
                            if (error is CancellationException) throw error
                            dispatch(Msg.Failed(error.message ?: "Failed to load recommendations"))
                        }
                        .collect { dispatch(Msg.ItemsLoaded(it)) }
                }
        }
    }

    private object ReducerImpl : Reducer<RecommendationStore.State, Msg> {
        override fun RecommendationStore.State.reduce(msg: Msg): RecommendationStore.State =
            when (msg) {
                Msg.Loading -> copy(loadState = RecommendationStore.LoadState.Loading)
                is Msg.ItemsLoaded -> copy(items = msg.items, loadState = RecommendationStore.LoadState.Idle)
                is Msg.Failed -> copy(loadState = RecommendationStore.LoadState.Failed(msg.message))
                is Msg.ConnectivityChanged -> copy(isOnline = msg.value)
                is Msg.LocalAvailabilityChanged -> copy(hasLocalContent = msg.value)
            }
    }
}
