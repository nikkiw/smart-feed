package com.core.data.dto

import com.core.database.content.ContentPreviewWithDetails
import com.core.database.content.ContentUpdateWithDetails
import com.core.domain.model.Content
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.model.ContentItemType
import com.core.domain.model.Embeddings
import com.core.domain.model.ImageUrl
import com.core.domain.model.ShortDescription
import com.core.domain.model.Tags
import com.core.domain.model.Title
import com.core.domain.model.UpdatedAt

fun ContentUpdateWithDetails.toContentItem(): ContentItem {
    val type = ContentItemType.Companion.fromString(contentUpdate.type)
    return when (type) {
        ContentItemType.ARTICLE -> ContentItem.Article(
            id = ContentItemId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            title = Title(article!!.title),
            content = Content(article!!.content),
            short = ShortDescription(article!!.shortDescription),
            embeddings = Embeddings(article!!.embeddings)
        )

        else -> ContentItem.Unknown(
            id = ContentItemId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            rawType = contentUpdate.type
        )
    }
}

fun ContentPreviewWithDetails.toContentPreview(): com.core.domain.model.ContentItemPreview {
    val type = ContentItemType.Companion.fromString(contentUpdate.type)
    return when (type) {
        ContentItemType.ARTICLE -> com.core.domain.model.ContentItemPreview.ArticlePreview(
            id = ContentItemId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            title = Title(article?.title ?: ""),
            short = ShortDescription(article?.shortDescription ?: "")
        )

        else -> com.core.domain.model.ContentItemPreview.UnknownPreview(
            id = ContentItemId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            rawType = contentUpdate.type
        )
    }
}