package com.feature.feed.mapper

import com.feature.feed.domain.model.ContentItem
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.root.ArticleRoutePreview

internal fun ContentItemPreview.toArticleRoutePreviewOrNull(): ArticleRoutePreview? = when (this) {
    is ContentItemPreview.ArticlePreview ->
        ArticleRoutePreview(
            id = id.value,
            title = title.value,
            shortDescription = short.value,
            updatedAtEpochMillis = updatedAt.epochMillis,
            mainImageUrl = mainImageUrl.value,
            tags = tags.value,
        )

    is ContentItemPreview.UnknownPreview -> null
}

internal fun ContentItem.Article.toArticleRoutePreview(): ArticleRoutePreview = ArticleRoutePreview(
    id = id.value,
    title = title.value,
    shortDescription = short.value,
    updatedAtEpochMillis = updatedAt.epochMillis,
    mainImageUrl = mainImageUrl.value,
    tags = tags.value,
)
