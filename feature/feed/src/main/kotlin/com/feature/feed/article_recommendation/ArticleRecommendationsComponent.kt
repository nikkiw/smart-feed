package com.feature.feed.article_recommendation

import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview

interface ArticleRecommendationsComponent {

    val items: Value<List<ContentItemPreview>>

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(itemId: ContentId)
}