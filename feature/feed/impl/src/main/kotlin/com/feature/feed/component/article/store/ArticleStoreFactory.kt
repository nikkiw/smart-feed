package com.feature.feed.component.article.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItem
import com.feature.feed.domain.usecase.GetContentItemUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class ArticleStoreFactory(
    private val storeFactory: StoreFactory,
    private val itemId: ContentId,
    private val getContentItemUseCase: GetContentItemUseCase,
) {
    fun create(): ArticleStore =
        object :
            ArticleStore,
            Store<ArticleStore.Intent, ArticleStore.State, ArticleStore.Label> by
            storeFactory.create(
                name = "ArticleStore",
                initialState = ArticleStore.State.Loading,
                bootstrapper = SimpleBootstrapper(Action.Bootstrap),
                executorFactory = ::ExecutorImpl,
                reducer = ReducerImpl,
            ) {}

    private sealed interface Action {
        data object Bootstrap : Action
    }

    private sealed interface Msg {
        data object Loading : Msg

        data class Loaded(val item: ContentItem) : Msg

        data class Failed(val message: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<ArticleStore.Intent, Action, ArticleStore.State, Msg, ArticleStore.Label>() {
        private var job: Job? = null

        override fun executeAction(action: Action) = load()

        override fun executeIntent(intent: ArticleStore.Intent) {
            when (intent) {
                ArticleStore.Intent.Retry -> load()
                ArticleStore.Intent.Close -> publish(ArticleStore.Label.Close)
            }
        }

        private fun load() {
            job?.cancel()
            dispatch(Msg.Loading)
            job =
                scope.launch {
                    getContentItemUseCase(itemId).fold(
                        onSuccess = { dispatch(Msg.Loaded(it)) },
                        onFailure = { dispatch(Msg.Failed(it.message ?: "Failed to load article")) },
                    )
                }
        }
    }

    private object ReducerImpl : Reducer<ArticleStore.State, Msg> {
        override fun ArticleStore.State.reduce(msg: Msg): ArticleStore.State =
            when (msg) {
                Msg.Loading -> ArticleStore.State.Loading
                is Msg.Loaded -> ArticleStore.State.Content(msg.item)
                is Msg.Failed -> ArticleStore.State.Failed(msg.message)
            }
    }
}
