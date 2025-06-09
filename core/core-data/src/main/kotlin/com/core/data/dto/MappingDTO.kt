package com.core.data.dto

import com.core.database.content.ContentUpdateWithDetails
import com.core.domain.model.Content
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.model.ContentItemType
import com.core.domain.model.ImageUrl
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
            title = Title(article?.title ?: ""),
            content = Content(article?.content ?: "")
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