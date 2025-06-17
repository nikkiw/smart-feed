package com.feature.feed.recommendation

import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview


/**
 *A component for displaying a recommendation of articles
 */
interface RecommendationListComponent {

    val items: Value<List<ContentItemPreview>>

    val isOnline: Boolean

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(itemId: ContentId)
}