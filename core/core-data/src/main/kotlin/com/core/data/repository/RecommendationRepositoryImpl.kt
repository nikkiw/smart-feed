package com.core.data.repository

import com.core.data.dto.toRecommendation
import com.core.database.recommendation.RecommendationDao
import com.core.di.IoDispatcher
import com.core.domain.model.ContentId
import com.core.domain.model.Recommendation
import com.core.domain.repository.RecommendationRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val recommendationDao: RecommendationDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : RecommendationRepository {
    override fun recommendForUser(): Flow<List<Recommendation>> =
        recommendationDao.getUserRecommendations()
            .map { listEntities ->
                listEntities.map {
                    it.toRecommendation()
                }
            }
            .flowOn(ioDispatcher)

    override suspend fun recommendForArticle(contentId: ContentId): List<Recommendation> {
        return recommendationDao.getContentRecommendations(contentId.value)
            .flowOn(ioDispatcher)
            .first()
            .map { it.toRecommendation() }
    }
}


