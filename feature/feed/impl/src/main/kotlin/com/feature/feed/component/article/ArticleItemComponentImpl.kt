package com.feature.feed.component.article

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.mvikotlin.core.binder.BinderLifecycleMode
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.bind
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.states
import com.core.analytics.api.AnalyticsService
import com.core.content.model.ContentId
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.component.article.store.ArticleStore
import com.feature.feed.component.article.store.ArticleStoreFactory
import com.feature.feed.component.articlerecommendation.ArticleRecommendationsComponentImpl
import com.feature.feed.domain.usecase.GetContentItemUseCase
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase

@Suppress("LongParameterList")
class ArticleItemComponentImpl(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    getContentItemUseCase: GetContentItemUseCase,
    recommendForArticleUseCase: RecommendForArticleUseCase,
    private val analyticsService: AnalyticsService,
    override val itemId: ContentId,
    private val onFinished: () -> Unit,
    onClickItem: (com.feature.feed.domain.model.ContentItemPreview) -> Unit,
) : ArticleItemComponent, ComponentContext by componentContext {
    private val stateHolder =
        instanceKeeper.getOrCreate(STATE_HOLDER_KEY) {
            StateHolder()
        }

    private val store =
        instanceKeeper.getStore {
            ArticleStoreFactory(storeFactory, itemId, getContentItemUseCase).create()
        }

    private val _model = MutableValue(ArticleItemComponent.Model())
    override val model: Value<ArticleItemComponent.Model> = _model
    override val initialScrollPosition: ArticleItemComponent.ScrollPosition
        get() =
            ArticleItemComponent.ScrollPosition(
                itemIndex = stateHolder.scrollItemIndex,
                itemOffsetPx = stateHolder.scrollItemOffsetPx,
            )

    override val articleRecommendationsComponent: ArticleRecommendationsComponent =
        ArticleRecommendationsComponentImpl(
            componentContext = childContext(key = "articleRecommendations"),
            storeFactory = storeFactory,
            articleId = itemId,
            recommendForArticleUseCase = recommendForArticleUseCase,
            onItemClick = onClickItem,
        )

    private var visibleStartTimestamp: Long? = null
    private var accumulatedVisibleMillis = 0L
    private var maxPercentRead = 0f

    init {
        bind(lifecycle, BinderLifecycleMode.CREATE_DESTROY) {
            store.states bindTo ::render
            store.labels bindTo { onFinished() }
        }
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onResume() {
                    visibleStartTimestamp = System.currentTimeMillis()
                }

                override fun onPause() {
                    stopReadingTimer()
                }

                override fun onDestroy() {
                    stopReadingTimer()
                    analyticsService.trackEventReadContent(itemId, accumulatedVisibleMillis, maxPercentRead)
                }
            },
        )
    }

    override fun onClose() = store.accept(ArticleStore.Intent.Close)

    override fun onRetry() = store.accept(ArticleStore.Intent.Retry)

    override fun onReadProgressChanged(percentRead: Float) {
        maxPercentRead = maxOf(maxPercentRead, percentRead.coerceIn(0f, 1f))
    }

    override fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int) {
        if (
            stateHolder.scrollItemIndex == itemIndex &&
            stateHolder.scrollItemOffsetPx == itemOffsetPx
        ) {
            return
        }

        stateHolder.scrollItemIndex = itemIndex
        stateHolder.scrollItemOffsetPx = itemOffsetPx
    }

    private fun stopReadingTimer() {
        visibleStartTimestamp?.let { accumulatedVisibleMillis += System.currentTimeMillis() - it }
        visibleStartTimestamp = null
    }

    private fun render(state: ArticleStore.State) {
        _model.value =
            ArticleItemComponent.Model(
                contentState =
                when (state) {
                    ArticleStore.State.Loading -> ArticleItemComponent.ContentState.Loading
                    is ArticleStore.State.Content -> ArticleItemComponent.ContentState.Content(state.item)
                    is ArticleStore.State.Failed -> ArticleItemComponent.ContentState.Failed(state.message)
                },
            )
    }

    private class StateHolder(
        var scrollItemIndex: Int = 0,
        var scrollItemOffsetPx: Int = 0,
    ) : InstanceKeeper.Instance

    private companion object {
        const val STATE_HOLDER_KEY = "ArticleItemUiStateHolder"
    }
}
