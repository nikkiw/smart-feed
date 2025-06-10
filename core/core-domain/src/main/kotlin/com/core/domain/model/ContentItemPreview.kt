package com.core.domain.model


sealed class ContentItemPreview {
    abstract val id: ContentItemId
    abstract val type: ContentItemType
    abstract val updatedAt: UpdatedAt
    abstract val mainImageUrl: ImageUrl
    abstract val tags: Tags

    data class ArticlePreview(
        override val id: ContentItemId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val title: Title,
        val short: ShortDescription
    ) : ContentItemPreview() {
        override val type: ContentItemType = ContentItemType.ARTICLE
    }

    data class UnknownPreview(
        override val id: ContentItemId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val rawType: String
    ) : ContentItemPreview() {
        override val type: ContentItemType = ContentItemType.UNKNOWN
    }
}
