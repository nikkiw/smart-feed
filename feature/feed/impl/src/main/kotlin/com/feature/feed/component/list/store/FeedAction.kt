package com.feature.feed.component.list.store

internal sealed interface FeedAction {
    data object Bootstrap : FeedAction
}
