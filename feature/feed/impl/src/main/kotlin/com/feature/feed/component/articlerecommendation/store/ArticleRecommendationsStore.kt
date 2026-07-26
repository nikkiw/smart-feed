package com.feature.feed.component.articlerecommendation.store

import com.arkivanov.mvikotlin.core.store.Store
import com.feature.feed.domain.model.ContentItemPreview

internal interface ArticleRecommendationsStore :
    Store<ArticleRecommendationsStore.Intent, ArticleRecommendationsStore.State, ArticleRecommendationsStore.Label> {
    sealed interface Intent {
        data object Retry : Intent

        data class ArticleClicked(val preview: ContentItemPreview) : Intent
    }

    sealed interface State {
        data object Loading : State

        data class Content(val items: List<ContentItemPreview>) : State

        data object Empty : State

        data class Failed(val message: String) : State
    }

    sealed interface Label {
        data class OpenArticle(val preview: ContentItemPreview) : Label
    }
}
