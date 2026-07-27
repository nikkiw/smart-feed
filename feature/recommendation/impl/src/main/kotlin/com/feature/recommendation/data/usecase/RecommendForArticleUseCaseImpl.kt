package com.feature.recommendation.data.usecase

import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.model.toContentItemPreview
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.recommendation.domain.repository.RecommendationRepository
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Default implementation of [RecommendForArticleUseCase].
 *
 * This class retrieves content recommendations for a given article by combining data from
 * [RecommendationRepository] and [ContentItemRepository]. It maps raw recommendation data
 * into displayable previews using the content repository.
 *
 * ### Example usage:
 * ```kotlin
 * recommendForArticleUseCase(contentId).collect { relatedArticles ->
 *     adapter.submitList(relatedArticles)
 * }
 * ```
 *
 * @param recommendationRepository Repository that provides recommendation data based on article ID.
 * @param contentItemRepository Repository used to fetch full content items and convert them to previews.
 * @see RecommendForArticleUseCase for interface definition
 */
class RecommendForArticleUseCaseImpl
@Inject
constructor(
    private val recommendationRepository: RecommendationRepository,
    private val contentItemRepository: ContentItemRepository,
) : RecommendForArticleUseCase {
    /**
     * Retrieves a list of recommended content items related to the specified article.
     *
     * 1. Fetches raw recommendations via [RecommendationRepository.recommendForArticle].
     * 2. For each recommended article ID, fetches the actual content item.
     * 3. Converts each content item into a preview using [toContentItemPreview].
     *
     * @param articleId The ID of the article for which to get recommendations.
     * @return A [Flow] emitting recommended content items whenever local data changes.
     */
    override fun invoke(articleId: ContentId): Flow<List<ContentItemPreview>> =
        recommendationRepository.recommendForArticle(articleId)
            .map { recommendations ->
                recommendations.mapNotNull { recommendation ->
                    contentItemRepository
                        .getContentById(recommendation.articleId)
                        .getOrNull()
                        ?.toContentItemPreview()
                }
            }
}
