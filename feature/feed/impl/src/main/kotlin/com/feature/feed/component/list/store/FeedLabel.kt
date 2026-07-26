package com.feature.feed.component.list.store

import com.feature.feed.domain.model.ContentItemPreview

sealed interface FeedLabel {
    data class OpenArticle(
        val preview: ContentItemPreview,
    ) : FeedLabel

    data object OpenInternetSettings : FeedLabel
}
