package com.feature.feed.component.list.store

import com.arkivanov.mvikotlin.core.store.Reducer

internal object FeedReducer : Reducer<FeedState, FeedMsg> {
    override fun FeedState.reduce(msg: FeedMsg): FeedState = when (msg) {
        is FeedMsg.QueryChanged -> copy(query = msg.query)
        is FeedMsg.ConnectivityChanged -> copy(isOnline = msg.isOnline)
        is FeedMsg.LocalAvailabilityChanged -> copy(hasLocalContent = msg.hasLocalContent)

        is FeedMsg.RefreshStarted ->
            copy(refreshState = FeedState.RefreshState.Refreshing(msg.requestId))

        is FeedMsg.RefreshCompleted ->
            if (refreshState.requestId == msg.requestId) {
                copy(refreshState = FeedState.RefreshState.Idle)
            } else {
                this
            }

        is FeedMsg.RefreshFailed ->
            if (refreshState.requestId == msg.requestId) {
                copy(
                    refreshState =
                    FeedState.RefreshState.Failed(
                        requestId = msg.requestId,
                        message = msg.message,
                    ),
                )
            } else {
                this
            }
    }

    private val FeedState.RefreshState.requestId: Long?
        get() =
            when (this) {
                FeedState.RefreshState.Idle -> null
                is FeedState.RefreshState.Refreshing -> requestId
                is FeedState.RefreshState.Failed -> requestId
            }
}
