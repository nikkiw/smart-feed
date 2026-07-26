package com.feature.feed.recommendation

import com.arkivanov.decompose.value.Value
import com.feature.feed.domain.model.ContentItemPreview
import kotlinx.coroutines.flow.Flow

/**
 *A component for displaying a recommendation of articles
 */
interface RecommendationListComponent {
    val model: Value<Model>
    val initialScrollPosition: ScrollPosition
    val effects: Flow<Effect>

    fun onRetry()

    fun onEnableInternetClicked()

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(item: ContentItemPreview)

    fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int)

    data class Model(
        val items: List<ContentItemPreview> = emptyList(),
        val isOnline: Boolean,
        val hasLocalContent: Boolean = false,
        val loadState: LoadState = LoadState.Loading,
    )

    data class ScrollPosition(
        val itemIndex: Int = 0,
        val itemOffsetPx: Int = 0,
    )

    sealed interface LoadState {
        data object Loading : LoadState

        data object Idle : LoadState

        data class Failed(val message: String) : LoadState
    }

    sealed interface Effect {
        data object OpenInternetSettings : Effect
    }
}
