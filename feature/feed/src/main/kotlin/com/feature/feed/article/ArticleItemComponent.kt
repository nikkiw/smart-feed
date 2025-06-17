package com.feature.feed.article


import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentItem
import com.feature.feed.article_recommendation.ArticleRecommendationsComponent

/**
 * A component for displaying a single article (ribbon item).
 */
interface ArticleItemComponent {

    /**
     * UI state for the element.
     */
    val model: Value<State>

    val articleRecommendationsComponent: ArticleRecommendationsComponent

    sealed class State {
        data object Init : State()
        data class Loaded(val contentItem: ContentItem) : State()
        data class Error(val errorMessage: String) : State()
    }


    fun onClose()

    fun registerOnCloseListener(listener: () -> Unit)

    fun logPercentRead(percentRead: Float)

}