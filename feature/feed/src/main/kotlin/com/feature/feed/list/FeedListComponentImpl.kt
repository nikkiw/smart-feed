package com.feature.feed.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.repository.Query
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Implementation of the Feed List Component with support for Paging and Pull-to-Refresh.
 */
class FeedListComponentImpl(
    componentContext: ComponentContext,
    private val getContentUseCase: GetContentUseCase,
    private val syncContentUseCase: SyncContentUseCase,
    initialQuery: Query,
    private val onItemClick: (ContentItemId) -> Unit
) : FeedListComponent, ComponentContext by componentContext {

    private val _pagingItems = MutableValue<PagingData<ContentItem>>(PagingData.empty())
    override val pagingItems: Value<PagingData<ContentItem>> = _pagingItems

    private val _isRefreshing = MutableValue(false)
    override val isRefreshing: Value<Boolean> = _isRefreshing

    private var currentQuery: Query = initialQuery

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var pagingJob: Job? = null
    private var refreshJob: Job? = null

    init {
        loadContent(currentQuery)
    }

    private fun loadContent(query: Query) {
        pagingJob?.cancel()
        pagingJob = scope.launch {
            getContentUseCase(query)
                .cachedIn(this)
                .collectLatest {
                    _pagingItems.value = it
                    _isRefreshing.value = false
                }
        }
    }

    override fun onRefresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            _isRefreshing.value = true
            syncContentUseCase.invoke()
            _isRefreshing.value = false
        }
    }

    override fun onListItemClick(itemId: ContentItemId) {
        onItemClick(itemId)
    }

    fun updateQuery(newQuery: Query) {
        if (newQuery != currentQuery) {
            currentQuery = newQuery
            loadContent(currentQuery)
        }
    }
}