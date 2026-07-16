package com.feature.feed.component.recommendation.store

import com.arkivanov.mvikotlin.core.store.Store
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview

internal interface RecommendationStore :
    Store<RecommendationStore.Intent, RecommendationStore.State, RecommendationStore.Label> {
    sealed interface Intent {
        data object Retry : Intent

        data object EnableInternetClicked : Intent

        data class ArticleClicked(val id: ContentId) : Intent
    }

    data class State(
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

    sealed interface Label {
        data class OpenArticle(val id: ContentId) : Label

        data object OpenInternetSettings : Label
    }
}
