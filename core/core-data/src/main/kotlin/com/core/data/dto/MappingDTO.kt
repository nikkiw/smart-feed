package com.core.data.dto

import com.core.database.content.entity.ContentPreviewWithDetails
import com.core.database.content.entity.ContentWithDetails
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.entity.UserRecommendationEntity
import com.core.domain.model.Content
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentId
import com.core.domain.model.ContentType
import com.core.domain.model.ImageUrl
import com.core.domain.model.Recommendation
import com.core.domain.model.ShortDescription
import com.core.domain.model.Tags
import com.core.domain.model.Title
import com.core.domain.model.UpdatedAt

fun ContentWithDetails.toContentItem(): ContentItem {
    val type = ContentType.Companion.fromString(contentUpdate.type)
    return when (type) {
        ContentType.ARTICLE -> ContentItem.Article(
            id = ContentId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            title = Title(article!!.title),
            content = Content(article!!.content),
            short = ShortDescription(article!!.shortDescription),
        )

        else -> ContentItem.Unknown(
            id = ContentId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            rawType = contentUpdate.type
        )
    }
}

fun ContentPreviewWithDetails.toContentPreview(): com.core.domain.model.ContentItemPreview {
    val type = ContentType.Companion.fromString(contentUpdate.type)
    return when (type) {
        ContentType.ARTICLE -> com.core.domain.model.ContentItemPreview.ArticlePreview(
            id = ContentId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            title = Title(article?.title ?: ""),
            short = ShortDescription(article?.shortDescription ?: "")
        )

        else -> com.core.domain.model.ContentItemPreview.UnknownPreview(
            id = ContentId(contentUpdate.id),
            updatedAt = UpdatedAt(contentUpdate.updatedAt),
            mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
            tags = Tags(contentUpdate.tags),
            rawType = contentUpdate.type
        )
    }
}

fun UserRecommendationEntity.toRecommendation(): Recommendation {
    return Recommendation(
        articleId = ContentId(recommendedContentId),
        score = score
    )
}


fun ContentRecommendationEntity.toRecommendation(): Recommendation {
    return Recommendation(
        articleId = ContentId(recommendedContentId),
        score = score
    )
}