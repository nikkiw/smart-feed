package com.feature.feed.component.list.store

import com.core.content.model.ContentId

sealed interface FeedLabel {
    data class OpenArticle(
        val contentId: ContentId,
    ) : FeedLabel

    data object OpenInternetSettings : FeedLabel
}
