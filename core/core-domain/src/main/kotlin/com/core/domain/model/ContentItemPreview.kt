package com.core.domain.model


/**
 * Base sealed class representing a preview of a content item.
 *
 * A preview contains only the essential information about a content item,
 * suitable for listing or summary views in UI or APIs.
 *
 * @property id Unique identifier of the content item.
 * @property type Type of the content (e.g., article, unknown).
 * @property updatedAt Timestamp indicating when the content was last updated.
 * @property mainImageUrl Main image URL associated with the content.
 * @property tags Tags associated with the content item.
 */
sealed class ContentItemPreview {
    abstract val id: ContentId
    abstract val type: ContentType
    abstract val updatedAt: UpdatedAt
    abstract val mainImageUrl: ImageUrl
    abstract val tags: Tags


    /**
     * Represents a preview of an article content type.
     *
     * @property id Unique ID of the article.
     * @property updatedAt Timestamp when the article was last updated.
     * @property mainImageUrl Main image URL of the article.
     * @property tags List of tags associated with the article.
     * @property title Title of the article.
     * @property short Short description or summary of the article.
     */
    data class ArticlePreview(
        override val id: ContentId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val title: Title,
        val short: ShortDescription
    ) : ContentItemPreview() {
        override val type: ContentType = ContentType.ARTICLE
    }

    /**
     * Represents a preview of an unrecognized or unknown content type.
     *
     * @property id Unique ID of the content.
     * @property updatedAt Timestamp when the content was last updated.
     * @property mainImageUrl Main image URL of the content.
     * @property tags List of tags associated with the content.
     * @property rawType Raw string value representing the unrecognized content type.
     */
    data class UnknownPreview(
        override val id: ContentId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val rawType: String
    ) : ContentItemPreview() {
        override val type: ContentType = ContentType.UNKNOWN
    }
}
