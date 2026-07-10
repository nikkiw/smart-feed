package com.feature.feed.store

import com.feature.feed.domain.repository.Query

sealed interface FeedAction {
    data class StartLoad(
        val requestId: Long,
        val query: Query,
    ) : FeedAction

    data class CancelLoad(
        val requestId: Long,
    ) : FeedAction

    data class StartRefresh(
        val requestId: Long,
    ) : FeedAction

    data class CancelRefresh(
        val requestId: Long,
    ) : FeedAction
}
