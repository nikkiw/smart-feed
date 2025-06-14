package com.core.domain.model


sealed class ContentItem {
    abstract val id: ContentItemId
    abstract val type: ContentItemType
    abstract val updatedAt: UpdatedAt
    abstract val mainImageUrl: ImageUrl
    abstract val tags: Tags


    data class Article(
        override val id: ContentItemId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val title: Title,
        val short: ShortDescription,
        val content: Content,
        val embeddings: Embeddings,
    ) : ContentItem() {
        override val type: ContentItemType = ContentItemType.ARTICLE
    }

    data class Unknown(
        override val id: ContentItemId,
        override val updatedAt: UpdatedAt,
        override val mainImageUrl: ImageUrl,
        override val tags: Tags,
        val rawType: String
    ) : ContentItem() {
        override val type: ContentItemType = ContentItemType.UNKNOWN
    }
}
