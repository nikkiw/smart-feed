package com.feature.feed.component.list.ui.compose

import com.feature.feed.domain.model.ContentItemPreview

data class ArticleCardUiModel(
    val id: String,
    val imageUrl: String,
    val title: String,
    val shortDescription: String,
    val date: String,
    val tags: List<String>,
)

internal fun ContentItemPreview.ArticlePreview.toArticleCardUiModel() =
    ArticleCardUiModel(
        id = id.toString(),
        imageUrl = mainImageUrl.value,
        title = title.value,
        shortDescription = short.value,
        date = updatedAt.toString(),
        tags = tags.value.toList(),
    )
