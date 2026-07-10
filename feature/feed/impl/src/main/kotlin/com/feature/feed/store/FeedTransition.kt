package com.feature.feed.store

data class FeedTransition(
    val state: FeedState,
    val actions: List<FeedAction> = emptyList(),
    val labels: List<FeedLabel> = emptyList(),
)
