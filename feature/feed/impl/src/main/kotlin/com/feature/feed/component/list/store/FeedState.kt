package com.feature.feed.component.list.store

import com.feature.feed.domain.repository.Query

data class FeedState(
    val query: Query,
    val isOnline: Boolean,
    val hasLocalContent: Boolean = false,
    val refreshState: RefreshState = RefreshState.Idle,
) {
    sealed interface RefreshState {
        data object Idle : RefreshState

        data class Refreshing(
            val requestId: Long,
        ) : RefreshState

        data class Failed(
            val requestId: Long,
            val message: String,
        ) : RefreshState
    }
}
