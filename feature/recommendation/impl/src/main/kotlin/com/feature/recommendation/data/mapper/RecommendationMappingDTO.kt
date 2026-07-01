package com.feature.recommendation.data.mapper

import com.core.content.model.ContentId
import com.feature.recommendation.domain.model.Recommendation
import com.feature.recommendation.local.recommendation.entity.ContentRecommendationEntity
import com.feature.recommendation.local.recommendation.entity.UserRecommendationEntity

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
