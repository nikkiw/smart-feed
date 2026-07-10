package com.feature.feed.store

import com.feature.feed.domain.repository.Query

data class FeedState(
    val query: Query,
    val loadState: LoadState = LoadState.Idle,
    val refreshState: RefreshState = RefreshState.Idle,
) {
    val isLoading: Boolean
        get() = loadState is LoadState.Loading

    val isRefreshing: Boolean
        get() = refreshState is RefreshState.Refreshing

    val loadErrorMessage: String?
        get() = (loadState as? LoadState.Failed)?.message

    val refreshErrorMessage: String?
        get() = (refreshState as? RefreshState.Failed)?.message

    sealed interface LoadState {
        data object Idle : LoadState

        data class Loading(
            val requestId: Long,
        ) : LoadState

        data class Failed(
            val requestId: Long,
            val message: String,
        ) : LoadState
    }

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
