package com.feature.feed.root

import kotlinx.serialization.Serializable

@Serializable
data class ArticleRoutePreview(
    val id: String,
    val title: String,
    val shortDescription: String,
    val updatedAtEpochMillis: Long,
    val mainImageUrl: String,
    val tags: List<String>,
)
