package com.core.data.dto

import com.core.content.model.ContentId
import com.core.database.recommendation.entity.ContentRecommendationEntity
import com.core.database.recommendation.entity.UserRecommendationEntity
import com.feature.recommendation.domain.model.Recommendation

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
