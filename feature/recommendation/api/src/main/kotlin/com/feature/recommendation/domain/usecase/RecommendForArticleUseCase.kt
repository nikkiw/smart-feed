package com.feature.recommendation.domain.usecase

import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview
import kotlinx.coroutines.flow.Flow

/**
 * Use case interface for retrieving content recommendations based on a specific article.
 *
 * This interface provides a way to fetch related content items (e.g., similar articles)
 * that can be shown to the user in a recommendation section such as "You might also like".
 *
 * ### Example usage:
 * ```kotlin
 * recommendForArticleUseCase(articleId).collect { recommendations ->
 *     showRelatedArticles(recommendations)
 * }
 * ```
 */
interface RecommendForArticleUseCase {
    /**
     * Retrieves a list of recommended content items related to the specified article.
     *
     * @param articleId The ID of the article for which to get recommendations.
     * @return A [Flow] emitting recommended content items whenever local data changes.
     */
    operator fun invoke(articleId: ContentId): Flow<List<ContentItemPreview>>
}
