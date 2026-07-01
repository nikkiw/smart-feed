package com.feature.feed.article

import com.arkivanov.decompose.value.Value
import com.core.content.model.ContentId
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.domain.model.ContentItem

/**
 * A component for displaying a single article (ribbon item).
 */
interface ArticleItemComponent {
    /**
     * UI state for the element.
     */
    val model: Value<State>

    val itemId: ContentId

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
