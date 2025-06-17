package com.core.domain.model

import com.core.domain.model.ContentItemPreview.*

/**
 * Base sealed class representing a content item in the system.
 *
 * @property id Unique identifier of the content item.
 * @property type Type of the content (e.g., article, unknown).
 * @property updatedAt Timestamp indicating when the content was last updated.
 * @property mainImageUrl Main image URL associated with the content.
 * @property tags Tags associated with the content item.
 */
sealed class ContentItem {
    abstract val id: ContentId
    abstract val type: ContentType
    abstract val updatedAt: UpdatedAt
    abstract val mainImageUrl: ImageUrl
    abstract val tags: Tags


    /**
     * Represents an article — a specific type of content.
     *
     * @property id Unique ID of the article.
     * @property updatedAt Timestamp when the article was last updated.
     * @property mainImageUrl Main image URL of the article.
     * @property tags List of tags associated with the article.
     * @property title Title of the article.
     * @property short Short description of the article.
     * @property content Full text content of the article.
     */
    data class Article(
        override val id: ContentId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val title: Title,
        val short: ShortDescription,
        val content: Content
    ) : ContentItem() {
        override val type: ContentType = ContentType.ARTICLE
    }

    /**
     * Represents an unknown content type that could not be recognized by the system.
     *
     * @property id Unique ID of the content.
     * @property updatedAt Timestamp when the content was last updated.
     * @property mainImageUrl Main image URL of the content.
     * @property tags List of tags associated with the content.
     * @property rawType Raw string value of the unrecognized content type.
     */
    data class Unknown(
        override val id: ContentId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val rawType: String
    ) : ContentItem() {
        override val type: ContentType = ContentType.UNKNOWN
    }
}

/**
 * Converts a [ContentItem] into a preview object ([ContentItemPreview]).
 *
 * This is useful for displaying summaries or lists of content items without exposing full details.
 *
 * @return A preview version of the content item.
 */
fun ContentItem.toContentItemPreview(): ContentItemPreview {
    return when (this) {
        is ContentItem.Article -> ArticlePreview(
            id = id,
            updatedAt = updatedAt,
            mainImageUrl = mainImageUrl,
            tags = tags,
            title = title,
            short = short
        )

        is ContentItem.Unknown -> UnknownPreview(
            id = id,
            updatedAt = updatedAt,
            mainImageUrl = mainImageUrl,
            tags = tags,
            rawType = type.toString()
        )
    }
}