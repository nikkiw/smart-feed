package com.core.data.usecase.recommendation

import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview
import com.core.domain.model.toContentItemPreview
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.RecommendationRepository
import com.core.domain.usecase.recommendation.RecommendForArticleUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * val relatedArticles = recommendForArticleUseCase(contentId)
 * if (relatedArticles.isNotEmpty()) {
 *     adapter.submitList(relatedArticles)
 * }
 * ```
 *
 * @param recommendationRepository Repository that provides recommendation data based on article ID.
 * @param contentItemRepository Repository used to fetch full content items and convert them to previews.
 * @see RecommendForArticleUseCase for interface definition
 */
class RecommendForArticleUseCaseImpl @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val contentItemRepository: ContentItemRepository
) : RecommendForArticleUseCase {

    /**
     * Retrieves a list of recommended content items related to the specified article.
     *
     * 1. Fetches raw recommendations via [RecommendationRepository.recommendForArticle].
     * 2. For each recommended article ID, fetches the actual content item.
     * 3. Converts each content item into a preview using [com.core.domain.model.ContentItem.toContentItemPreview].
     *
     * @param articleId The ID of the article for which to get recommendations.
     * @return A list of [ContentItemPreview] objects representing recommended content.
     *         Returns an empty list if no recommendations can be retrieved or mapped.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun invoke(articleId: ContentId): List<ContentItemPreview> {
        return recommendationRepository.recommendForArticle(articleId)
            .mapNotNull { recommendation ->
                contentItemRepository
                    .getContentById(recommendation.articleId)
                    .getOrNull()
                    ?.toContentItemPreview()
            }
    }
}