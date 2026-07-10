package com.feature.feed.store

import com.core.content.model.ContentId

sealed interface FeedLabel {
    data class OpenArticle(
        val contentId: ContentId,
    ) : FeedLabel

    data class ShowMessage(
        val message: String,
    ) : FeedLabel
}
