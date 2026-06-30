package com.core.data.dto

import com.feature.feed.local.content.entity.ContentPreviewWithDetails
import com.feature.feed.local.content.entity.ContentWithDetails
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.entity.UserRecommendationEntity
import com.core.content.model.Content
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItem
import com.core.content.model.ContentType
import com.core.content.model.ImageUrl
import com.feature.recommendation.domain.model.Recommendation
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt

fun ContentWithDetails.toContentItem(): ContentItem {
    val type = ContentType.Companion.fromString(contentUpdate.type)
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

fun ContentPreviewWithDetails.toContentPreview(): com.feature.feed.domain.model.ContentItemPreview {
    val type = ContentType.Companion.fromString(contentUpdate.type)
    return when (type) {
        ContentType.ARTICLE ->
            com.feature.feed.domain.model.ContentItemPreview.ArticlePreview(
                id = ContentId(contentUpdate.id),
                updatedAt = UpdatedAt(contentUpdate.updatedAt),
                mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
                tags = Tags(contentUpdate.tags),
                title = Title(article?.title ?: ""),
                short = ShortDescription(article?.shortDescription ?: ""),
            )

        else ->
            com.feature.feed.domain.model.ContentItemPreview.UnknownPreview(
                id = ContentId(contentUpdate.id),
                updatedAt = UpdatedAt(contentUpdate.updatedAt),
                mainImageUrl = ImageUrl(contentUpdate.mainImageUrl),
                tags = Tags(contentUpdate.tags),
                rawType = contentUpdate.type,
            )
    }
}

fun UserRecommendationEntity.toRecommendation(): Recommendation {
    return Recommendation(
        articleId = ContentId(recommendedContentId),
        score = score,
    )
}

fun ContentRecommendationEntity.toRecommendation(): Recommendation {
    return Recommendation(
        articleId = ContentId(recommendedContentId),
        score = score,
    )
}
