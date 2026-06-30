package com.feature.feed.articlerecommendation

import com.arkivanov.decompose.value.Value
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview

interface ArticleRecommendationsComponent {
    val items: Value<List<ContentItemPreview>>

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(itemId: ContentId)
}
