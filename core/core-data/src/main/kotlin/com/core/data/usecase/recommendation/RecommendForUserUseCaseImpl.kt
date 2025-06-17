package com.core.data.usecase.recommendation

import com.core.domain.model.ContentItemPreview
import com.core.domain.model.toContentItemPreview
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.RecommendationRepository
import com.core.domain.usecase.recommendation.RecommendForUserUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

/**
 * Default implementation of [RecommendForUserUseCase].
 *
 * This class retrieves a stream of personalized content recommendations for the current user
 * by combining data from [RecommendationRepository] and [ContentItemRepository].
 *
 * It transforms raw recommendation data into displayable previews using the content repository.
 *
 * ### Example usage:
 * ```kotlin
 * recommendForUserUseCase().launchAndCollectIn(viewModelScope) {
 *     adapter.submitList(it)
 * }
 * ```
 *
 * @param recommendationRepository Repository that provides user-based recommendation data.
 * @param contentItemRepository Repository used to fetch full content items and convert them to previews.
 * @see RecommendForUserUseCase for interface definition
 */
class RecommendForUserUseCaseImpl @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val contentItemRepository: ContentItemRepository
) : RecommendForUserUseCase {

    /**
     * Retrieves a [Flow] of recommended content items for the current user.
     *
     * 1. Fetches a flow of raw [com.core.domain.model.Recommendation] objects via [RecommendationRepository.recommendForUser].
     * 2. For each list of recommendations, maps them to previewable content items.
     * 3. Skips any articles that cannot be fetched or converted to previews.
     *
     * Uses [mapLatest] to ensure only the most recent set of recommendations is processed,
     * cancelling any previous transformations.
     *
     * @return A [Flow] emitting lists of [ContentItemPreview] objects.
     *         Returns empty lists if no recommendations can be retrieved or mapped.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override operator fun invoke(): Flow<List<ContentItemPreview>> =
        recommendationRepository.recommendForUser()
            .mapLatest { recommendations ->
                recommendations.mapNotNull { rec ->
                    contentItemRepository
                        .getContentById(rec.articleId)
                        .getOrNull()
                        ?.toContentItemPreview()
                }
            }
}