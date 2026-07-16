package com.feature.feed.component.list.store

import com.feature.feed.domain.repository.Query

internal sealed interface FeedMsg {
    data class QueryChanged(
        val query: Query,
    ) : FeedMsg

    data class ConnectivityChanged(
        val isOnline: Boolean,
    ) : FeedMsg

    data class LocalAvailabilityChanged(
        val hasLocalContent: Boolean,
    ) : FeedMsg

    data class RefreshCompleted(
        val requestId: Long,
    ) : FeedMsg

    data class RefreshFailed(
        val requestId: Long,
        val message: String,
    ) : FeedMsg

    data class RefreshStarted(
        val requestId: Long,
    ) : FeedMsg
}
