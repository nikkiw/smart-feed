package com.feature.feed.list

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview
import com.core.domain.repository.Query
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import com.core.observers.ConnectivityRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Implementation of the Feed List Component with support for Paging and Pull-to-Refresh.
 */
class FeedListComponentImpl(
    componentContext: ComponentContext,
    private val getContentUseCase: GetContentUseCase,
    private val syncContentUseCase: SyncContentUseCase,
    private val connectivityRepository: ConnectivityRepository,
    initialQuery: Query,
    private val onItemClick: (ContentId) -> Unit
) : FeedListComponent, ComponentContext by componentContext {

    private val _pagingItems = MutableValue<PagingData<ContentItemPreview>>(PagingData.empty())
    override val pagingItems: Value<PagingData<ContentItemPreview>> = _pagingItems

    private val _isRefreshing =
        MutableValue<FeedListComponent.State>(FeedListComponent.State.RefreshSuccess)
    override val isRefreshing: Value<FeedListComponent.State> = _isRefreshing

    private var currentQuery: Query = initialQuery

//    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var pagingJob: Job? = null
    private var refreshJob: Job? = null

    override val isOnline: Boolean
        get() = connectivityRepository.isInternetAvailable()

    init {
        loadContent(currentQuery)
    }

    private fun loadContent(query: Query) {
        pagingJob?.cancel()
        pagingJob = coroutineScope()
            .launch {
                getContentUseCase(query)
                    .cachedIn(this)
                    .collectLatest {
                        _pagingItems.value = it
                    }
            }
    }

    override fun onRefresh() {
        refreshJob?.cancel()
        refreshJob = coroutineScope()
            .launch {
                _isRefreshing.value = FeedListComponent.State.IsRefreshing
                syncContentUseCase.invoke().onFailure {
                    _isRefreshing.value = FeedListComponent.State.ErrorRefresh(
                        it.message ?: "Failed refresh data (unknown error)"
                    )
                }.onSuccess {
                    _isRefreshing.value = FeedListComponent.State.RefreshSuccess
                }
            }
    }

    override fun onListItemClick(itemId: ContentId) {
        onItemClick(itemId)
    }

    fun updateQuery(newQuery: Query) {
        if (newQuery != currentQuery) {
            currentQuery = newQuery
            loadContent(currentQuery)
        }
    }
}