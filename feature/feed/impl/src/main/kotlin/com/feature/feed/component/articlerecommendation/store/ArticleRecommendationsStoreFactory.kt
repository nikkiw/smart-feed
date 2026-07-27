package com.feature.feed.component.articlerecommendation.store

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

internal class ArticleRecommendationsStoreFactory(
    private val storeFactory: StoreFactory,
    private val articleId: ContentId,
    private val recommendForArticleUseCase: RecommendForArticleUseCase,
) {
    fun create(): ArticleRecommendationsStore = object :
        ArticleRecommendationsStore,
        Store<
            ArticleRecommendationsStore.Intent,
            ArticleRecommendationsStore.State,
            ArticleRecommendationsStore.Label,
            > by
        storeFactory.create(
            name = "ArticleRecommendationsStore",
            initialState = ArticleRecommendationsStore.State.Loading,
            bootstrapper = SimpleBootstrapper(Action.Bootstrap),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data object Bootstrap : Action
    }

    private sealed interface Msg {
        data object Loading : Msg

        data class Loaded(val items: List<ContentItemPreview>) : Msg

        data class Failed(val message: String) : Msg
    }

    private inner class ExecutorImpl :
        CoroutineExecutor<
            ArticleRecommendationsStore.Intent,
            Action,
            ArticleRecommendationsStore.State,
            Msg,
            ArticleRecommendationsStore.Label,
            >() {
        private var job: Job? = null

        override fun executeAction(action: Action) = load()

        override fun executeIntent(intent: ArticleRecommendationsStore.Intent) {
            when (intent) {
                ArticleRecommendationsStore.Intent.Retry -> load()
                is ArticleRecommendationsStore.Intent.ArticleClicked ->
                    publish(ArticleRecommendationsStore.Label.OpenArticle(intent.preview))
            }
        }

        private fun load() {
            job?.cancel()
            dispatch(Msg.Loading)
            job =
                scope.launch {
                    recommendForArticleUseCase(articleId)
                        .catch { error ->
                            if (error is CancellationException) throw error
                            dispatch(Msg.Failed(error.message ?: "Failed to load related articles"))
                        }
                        .collect { dispatch(Msg.Loaded(it)) }
                }
        }
    }

    private object ReducerImpl : Reducer<ArticleRecommendationsStore.State, Msg> {
        override fun ArticleRecommendationsStore.State.reduce(msg: Msg): ArticleRecommendationsStore.State =
            when (msg) {
                Msg.Loading -> ArticleRecommendationsStore.State.Loading
                is Msg.Loaded ->
                    if (msg.items.isEmpty()) {
                        ArticleRecommendationsStore.State.Empty
                    } else {
                        ArticleRecommendationsStore.State.Content(msg.items)
                    }
                is Msg.Failed -> ArticleRecommendationsStore.State.Failed(msg.message)
            }
    }
}
