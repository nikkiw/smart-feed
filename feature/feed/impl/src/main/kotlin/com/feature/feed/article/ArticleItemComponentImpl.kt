package com.feature.feed.article

import android.util.Log
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.analytics.api.AnalyticsService
import com.core.content.model.ContentId
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponentImpl
import com.feature.feed.domain.usecase.GetContentItemUseCase
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import kotlinx.coroutines.launch

/**
 * ArticleItemComponent implementation with ComponentContext state and delegation.
 */
class ArticleItemComponentImpl(
    componentContext: ComponentContext,
    private val getContentItemUseCase: GetContentItemUseCase,
    private val recommendForArticleUseCase: RecommendForArticleUseCase,
    private val analyticsService: AnalyticsService,
    override val itemId: ContentId,
    private val onFinished: () -> Unit,
    private val onClickItem: (ContentId) -> Unit,
) : ArticleItemComponent, ComponentContext by componentContext {
    private val closeListeners = mutableListOf<() -> Unit>()

    private val _model =
        MutableValue<ArticleItemComponent.State>(ArticleItemComponent.State.Init)
    override val model = _model

    override val articleRecommendationsComponent: ArticleRecommendationsComponent =
        ArticleRecommendationsComponentImpl(
            componentContext = childContext(key = "articleRecommendations"),
            articleId = itemId,
            recommendForArticleUseCase = recommendForArticleUseCase,
            onItemClick = { id ->
                onClickItem(id)
            },
        )

//    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    // Когда экран появился
    private var visibleStartTimestamp: Long? = null

    // Сколько миллисекунд всего был видим
    private var accumulatedVisibleMillis: Long = 0

    private var percent = 0f

    override fun logPercentRead(percentRead: Float) {
        if (percentRead > percent) {
            percent = percentRead
        }
    }

    override fun onClose() {
        onFinished()
    }

    override fun registerOnCloseListener(listener: () -> Unit) {
        closeListeners += listener
    }

    init {
        // Подписываемся на события Decompose
        lifecycle.subscribe(
            object : Lifecycle.Callbacks {
                override fun onResume() {
                    super.onResume()
                    visibleStartTimestamp = System.currentTimeMillis()
                }

                override fun onPause() {
                    super.onPause()
                    visibleStartTimestamp?.let {
                        accumulatedVisibleMillis += System.currentTimeMillis() - it
                        visibleStartTimestamp = null
                    }
                }

                override fun onDestroy() {
                    closeListeners.forEach { it() }
                    logReadEvent()
                }
            },
        )

        coroutineScope()
            .launch {
                getContentItemUseCase.invoke(itemId).onFailure { action: Throwable ->
                    _model.value = ArticleItemComponent.State.Error(action.message ?: "UnknownError")
                }.onSuccess { contentItem ->
                    _model.value = ArticleItemComponent.State.Loaded(contentItem)
                }
            }
    }

    private fun logReadEvent() {
        Log.d(
            "logReadEvent",
            "itemId=$itemId, accumulatedVisibleMillis=$accumulatedVisibleMillis, percent=$percent",
        )
        analyticsService.trackEventReadContent(
            contentId = itemId,
            readingTimeMillis = accumulatedVisibleMillis,
            readPercentage = percent,
        )
    }
}
