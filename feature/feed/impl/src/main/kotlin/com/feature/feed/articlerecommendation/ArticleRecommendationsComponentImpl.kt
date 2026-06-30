package com.feature.feed.articlerecommendation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import kotlinx.coroutines.launch

class ArticleRecommendationsComponentImpl(
    componentContext: ComponentContext,
    private val articleId: ContentId,
    private val recommendForArticleUseCase: RecommendForArticleUseCase,
    private val onItemClick: (ContentId) -> Unit,
) : ArticleRecommendationsComponent, ComponentContext by componentContext {
    private val _items = MutableValue<List<ContentItemPreview>>(emptyList())
    override val items: Value<List<ContentItemPreview>> = _items

//    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    init {
        componentContext.coroutineScope().launch {
            _items.value = recommendForArticleUseCase.invoke(articleId)
        }
    }

    override fun onListItemClick(itemId: ContentId) {
        onItemClick(itemId)
    }
}
