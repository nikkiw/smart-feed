package com.feature.feed.list

import androidx.paging.PagingData
import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview

/**
 *A component for displaying a feed of articles with support for Paging and Pull-to-Refresh.
 */
interface FeedListComponent {

    /**
     * Feed status: PagingData stream.
     * Updated when filtering/sorting is changed.
     */
    val pagingItems: Value<PagingData<ContentItemPreview>>

    /**
     * Download status (for Pull-to-Refresh).
     */
    val isRefreshing: Value<State>

    /**
     * Request data again (Pull-to-Refresh).
     */
    fun onRefresh()

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(itemId: ContentId)


    val isOnline: Boolean

    sealed class State {
        data object IsRefreshing : State()
        data object RefreshSuccess : State()
        data class ErrorRefresh(val errorMessage: String) : State()
    }
}