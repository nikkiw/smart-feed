package com.feature.feed.list

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.Value
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.Query
import kotlinx.coroutines.flow.Flow

/**
 *A component for displaying a feed of articles with support for Paging and Pull-to-Refresh.
 */
interface FeedListComponent {
    /**
     * Feed status: PagingData stream.
     * Updated when filtering/sorting is changed.
     */
    val pagingItems: Flow<PagingData<ContentItemPreview>>

    /**
     * Download status (for Pull-to-Refresh).
     */
    val model: Value<Model>

    val initialScrollPosition: ScrollPosition

    /**
     * One-shot user-facing effects from the feed slice.
     */
    val effects: Flow<Effect>

    /**
     * Request data again (Pull-to-Refresh).
     */
    fun onRefresh()

    fun onRetry()

    fun onEnableInternetClicked()

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(item: ContentItemPreview)

    fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int)

    fun updateQuery(query: Query)

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            initialQuery: Query,
            onItemClick: (ContentItemPreview) -> Unit,
        ): FeedListComponent
    }

    data class Model(
        val isOnline: Boolean,
        val hasLocalContent: Boolean,
        val refreshState: RefreshState = RefreshState.Idle,
    )

    data class ScrollPosition(
        val itemIndex: Int = 0,
        val itemOffsetPx: Int = 0,
    )

    sealed interface RefreshState {
        data object Idle : RefreshState

        data object Refreshing : RefreshState

        data class Failed(val message: String) : RefreshState
    }

    sealed interface Effect {
        data object OpenInternetSettings : Effect
    }
}
