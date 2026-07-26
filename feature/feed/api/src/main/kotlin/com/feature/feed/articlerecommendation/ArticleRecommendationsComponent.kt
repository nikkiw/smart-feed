package com.feature.feed.articlerecommendation

import com.arkivanov.decompose.value.Value
import com.feature.feed.domain.model.ContentItemPreview

interface ArticleRecommendationsComponent {
    val model: Value<Model>

    fun onRetry()

    /**
     *The event when the user clicked on an item in the list
     */
    fun onListItemClick(item: ContentItemPreview)

    data class Model(
        val state: State = State.Loading,
    )

    sealed interface State {
        data object Loading : State

        data class Content(val items: List<ContentItemPreview>) : State

        data object Empty : State

        data class Failed(val message: String) : State
    }
}
