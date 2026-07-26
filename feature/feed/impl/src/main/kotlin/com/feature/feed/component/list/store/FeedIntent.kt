package com.feature.feed.component.list.store

import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.Query

sealed interface FeedIntent {
    data class QueryChanged(
        val query: Query,
    ) : FeedIntent

    data object RefreshRequested : FeedIntent

    data object RetryRequested : FeedIntent

    data object EnableInternetClicked : FeedIntent

    data class ArticleClicked(
        val preview: ContentItemPreview,
    ) : FeedIntent
}
