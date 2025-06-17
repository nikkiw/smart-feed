package com.core.domain.usecase.recommendation

import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview


/**
 * Use case interface for retrieving content recommendations based on a specific article.
 *
 * This interface provides a way to fetch related content items (e.g., similar articles)
 * that can be shown to the user in a recommendation section such as "You might also like".
 *
 * ### Example usage:
 * ```kotlin
 * val result = recommendForArticleUseCase(articleId)
 * if (result.isNotEmpty()) {
 *     showRelatedArticles(result)
 * }
 * ```
 */
interface RecommendForArticleUseCase {

    /**
     * Retrieves a list of recommended content items related to the specified article.
     *
     * @param articleId The ID of the article for which to get recommendations.
     * @return A list of [ContentItemPreview] objects representing recommended content.
     */
    suspend operator fun invoke(articleId: ContentId): List<ContentItemPreview>
}
