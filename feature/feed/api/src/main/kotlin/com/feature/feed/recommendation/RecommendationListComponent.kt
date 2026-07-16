package com.feature.feed.recommendation

import com.arkivanov.decompose.value.Value
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview
import kotlinx.coroutines.flow.Flow

/**
 *A component for displaying a recommendation of articles
 */
interface RecommendationListComponent {
    val model: Value<Model>
    val effects: Flow<Effect>

    fun onRetry()

    fun onEnableInternetClicked()

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(itemId: ContentId)

    data class Model(
        val items: List<ContentItemPreview> = emptyList(),
        val isOnline: Boolean,
        val hasLocalContent: Boolean = false,
        val loadState: LoadState = LoadState.Loading,
    )

    sealed interface LoadState {
        data object Loading : LoadState

        data object Idle : LoadState

        data class Failed(val message: String) : LoadState
    }

    sealed interface Effect {
        data object OpenInternetSettings : Effect
    }
}
