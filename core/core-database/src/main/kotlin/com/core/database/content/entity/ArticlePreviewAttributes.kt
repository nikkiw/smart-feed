package com.core.database.content.entity

/**
 * Lightweight view model of article data for previews.
 *
 * Includes only fields necessary for showing summaries in lists.
 *
 * @property contentId ID of the related content.
 * @property title Title of the article.
 * @property shortDescription Summary text.
 */
data class ArticlePreviewAttributes(
    val contentId: String,
    val title: String,
    val shortDescription: String
)
