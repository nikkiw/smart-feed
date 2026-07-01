package com.feature.feed.data.mapper

import com.core.content.model.Content
import com.core.content.model.ContentId
import com.core.content.model.ContentType
import com.core.content.model.ImageUrl
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt
import com.feature.feed.domain.model.ContentItem
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.local.content.entity.ContentPreviewWithDetails
import com.feature.feed.local.content.entity.ContentWithDetails

fun ContentWithDetails.toContentItem(): ContentItem {
    val type = ContentType.fromString(contentUpdate.type)
    return when (type) {
        ContentType.ARTICLE ->
            ContentItem.Article(
                id = ContentId(contentUpdate.id),
                updatedAt = UpdatedAt(contentUpdate.updatedAt),
                mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
                tags = Tags(contentUpdate.tags),
                title = Title(article!!.title),
                content = Content(article!!.content),
                short = ShortDescription(article!!.shortDescription),
            )

        else ->
            ContentItem.Unknown(
                id = ContentId(contentUpdate.id),
                updatedAt = UpdatedAt(contentUpdate.updatedAt),
                mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
                tags = Tags(contentUpdate.tags),
                rawType = contentUpdate.type,
            )
    }
}

fun ContentPreviewWithDetails.toContentPreview(): ContentItemPreview {
    val type = ContentType.fromString(contentUpdate.type)
    return when (type) {
        ContentType.ARTICLE ->
            ContentItemPreview.ArticlePreview(
                id = ContentId(contentUpdate.id),
                updatedAt = UpdatedAt(contentUpdate.updatedAt),
                mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
                tags = Tags(contentUpdate.tags),
                title = Title(article?.title ?: ""),
                short = ShortDescription(article?.shortDescription ?: ""),
            )

        else ->
            ContentItemPreview.UnknownPreview(
                id = ContentId(contentUpdate.id),
                updatedAt = UpdatedAt(contentUpdate.updatedAt),
                mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
                tags = Tags(contentUpdate.tags),
                rawType = contentUpdate.type,
            )
    }
}
