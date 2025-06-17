package com.feature.feed.recommendation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview
import com.core.domain.usecase.recommendation.RecommendForUserUseCase
import com.core.observers.ConnectivityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


/**
 * Implementation of the Recommendation List Component
 */
class RecommendationListComponentImpl(
    componentContext: ComponentContext,
    private val recommendForUserUseCase: RecommendForUserUseCase,
    private val connectivityRepository: ConnectivityRepository,
    private val onItemClick: (ContentId) -> Unit,
) : RecommendationListComponent, ComponentContext by componentContext {

    private val _items = MutableValue<List<ContentItemPreview>>(emptyList())
    override val items: Value<List<ContentItemPreview>> = _items

    override val isOnline: Boolean
        get() = connectivityRepository.isInternetAvailable()

//    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    init {
        coroutineScope().launch {
            recommendForUserUseCase()
                .collectLatest {
                    _items.value = it
                }
        }

    }

    override fun onListItemClick(itemId: ContentId) {
        onItemClick(itemId)
    }

}