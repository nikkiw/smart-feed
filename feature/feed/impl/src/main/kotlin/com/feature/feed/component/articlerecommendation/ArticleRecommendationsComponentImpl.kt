package com.feature.feed.component.articlerecommendation

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
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.component.articlerecommendation.store.ArticleRecommendationsStore
import com.feature.feed.component.articlerecommendation.store.ArticleRecommendationsStoreFactory
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase

class ArticleRecommendationsComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    articleId: ContentId,
    recommendForArticleUseCase: RecommendForArticleUseCase,
    private val onItemClick: (ContentItemPreview) -> Unit,
) : ArticleRecommendationsComponent, ComponentContext by componentContext {
    private val store =
        instanceKeeper.getStore {
            ArticleRecommendationsStoreFactory(
                storeFactory = storeFactory,
                articleId = articleId,
                recommendForArticleUseCase = recommendForArticleUseCase,
            ).create()
        }

    private val _model = MutableValue(ArticleRecommendationsComponent.Model())
    override val model: Value<ArticleRecommendationsComponent.Model> = _model

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.states bindTo ::render
            store.labels bindTo { label ->
                when (label) {
                    is ArticleRecommendationsStore.Label.OpenArticle -> onItemClick(label.preview)
                }
            }
        }
    }

    override fun onRetry() = store.accept(ArticleRecommendationsStore.Intent.Retry)

    override fun onListItemClick(item: ContentItemPreview) {
        store.accept(ArticleRecommendationsStore.Intent.ArticleClicked(item))
    }

    private fun render(state: ArticleRecommendationsStore.State) {
        _model.value =
            ArticleRecommendationsComponent.Model(
                state =
                when (state) {
                    ArticleRecommendationsStore.State.Loading -> ArticleRecommendationsComponent.State.Loading
                    is ArticleRecommendationsStore.State.Content ->
                        ArticleRecommendationsComponent.State.Content(state.items)
                    ArticleRecommendationsStore.State.Empty -> ArticleRecommendationsComponent.State.Empty
                    is ArticleRecommendationsStore.State.Failed ->
                        ArticleRecommendationsComponent.State.Failed(state.message)
                },
            )
    }
}
