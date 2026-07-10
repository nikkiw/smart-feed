package com.feature.feed.store

import com.core.content.model.ContentId
import com.feature.feed.domain.repository.Query

sealed interface FeedIntent {
    data class QueryChanged(
        val query: Query,
    ) : FeedIntent

    data object RefreshRequested : FeedIntent

    data object RetryRequested : FeedIntent

    data class ArticleClicked(
        val contentId: ContentId,
    ) : FeedIntent
}
