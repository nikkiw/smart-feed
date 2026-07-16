package com.feature.feed.component.article.store

import com.arkivanov.mvikotlin.core.store.Store
import com.feature.feed.domain.model.ContentItem

internal interface ArticleStore : Store<ArticleStore.Intent, ArticleStore.State, ArticleStore.Label> {
    sealed interface Intent {
        data object Retry : Intent

        data object Close : Intent
    }

    sealed interface State {
        data object Loading : State

        data class Content(val item: ContentItem) : State

        data class Failed(val message: String) : State
    }

    sealed interface Label {
        data object Close : Label
    }
}
