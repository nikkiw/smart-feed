package com.feature.feed.article

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.domain.model.ContentItemId
import com.core.domain.usecase.content.GetContentItemUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * ArticleItemComponent implementation with ComponentContext state and delegation.
 */
class ArticleItemComponentImpl(
    componentContext: ComponentContext,
    private val getContentItemUseCase: GetContentItemUseCase,
    private val itemId: ContentItemId,
    private val onFinished: () -> Unit
) : ArticleItemComponent, ComponentContext by componentContext {

    private val _model =
        MutableValue<ArticleItemComponent.State>(ArticleItemComponent.State.Init)
    override val model = _model

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onClose() {
        onFinished()
    }

    init {
        scope.launch {
            getContentItemUseCase.invoke(itemId).onFailure { action: Throwable ->
                _model.value = ArticleItemComponent.State.Error(action.message ?: "UnknownError")
            }.onSuccess { contentItem ->
                _model.value = ArticleItemComponent.State.Loaded(contentItem)
            }
        }
    }
}