package com.feature.feed.article

import com.arkivanov.decompose.value.Value
import com.core.content.model.ContentId
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.domain.model.ContentItem
import com.feature.feed.root.ArticleRoutePreview

/**
 * A component for displaying a single article (ribbon item).
 */
interface ArticleItemComponent {
    /**
     * UI state for the element.
     */
    val model: Value<Model>

    val itemId: ContentId

    val initialScrollPosition: ScrollPosition

    val articleRecommendationsComponent: ArticleRecommendationsComponent

    data class Model(
        val contentState: ContentState = ContentState.Loading,
        val routePreview: ArticleRoutePreview? = null,
    )

    data class ScrollPosition(
        val itemIndex: Int = 0,
        val itemOffsetPx: Int = 0,
    )

    data class ReadProgressSnapshot(
        val firstVisibleItemIndex: Int,
        val lastVisibleItemIndex: Int,
        val totalItemsCount: Int,
        val canScrollBackward: Boolean,
        val canScrollForward: Boolean,
        val viewportStartOffset: Int,
        val viewportEndOffset: Int,
        val bodyItemOffset: Int? = null,
        val bodyItemSize: Int? = null,
    )

    sealed interface ContentState {
        data object Loading : ContentState

        data class Content(val contentItem: ContentItem) : ContentState

        data class Failed(val message: String) : ContentState
    }

    fun onClose()

    fun onRetry()

    fun onReadProgressObserved(snapshot: ReadProgressSnapshot)

    fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int)
}
