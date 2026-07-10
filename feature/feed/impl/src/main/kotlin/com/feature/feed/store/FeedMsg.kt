package com.feature.feed.store

sealed interface FeedMsg {
    data class LoadCompleted(
        val requestId: Long,
    ) : FeedMsg

    data class LoadFailed(
        val requestId: Long,
        val message: String,
    ) : FeedMsg

    data class RefreshCompleted(
        val requestId: Long,
    ) : FeedMsg

    data class RefreshFailed(
        val requestId: Long,
        val message: String,
    ) : FeedMsg
}
