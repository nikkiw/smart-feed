package com.feature.feed.master

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentItemId
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.filter.FilterSortComponent
import com.feature.feed.filter.FilterSortComponentImpl
import com.feature.feed.list.FeedListComponent
import com.feature.feed.list.FeedListComponentImpl

/**
 * FeedRootComponent implementation.
 * Encapsulates the Filter Sort Component, Feed List Component, and the Article Item Component factory.
 */
class FeedMasterComponentImpl(
    componentContext: ComponentContext,
    private val contentItemRepository: ContentItemRepository,
    private val getContentUseCase: GetContentUseCase,
    private val syncContentUseCase: SyncContentUseCase,
    onListItemClick: (ContentItemId) -> Unit,
    initialSortType: ContentItemsSortedType = ContentItemsSortedType.ByDateNewestFirst,
    initialTags: Tags = Tags()
) : FeedMasterComponent, ComponentContext by componentContext {


    private val _state = MutableValue(
        FeedMasterComponent.State(
            selectedTags = initialTags,
            selectedSortType = initialSortType
        )
    )
    override val state: Value<FeedMasterComponent.State> = _state

    //We keep a link to the Feed List Component to update the query when changing the filter/sorting.
    override val feedListComponent: FeedListComponent

    override val filterSortComponent: FilterSortComponent

    init {
        filterSortComponent = FilterSortComponentImpl(
            componentContext = childContext(key = "filterSort"),
            contentItemRepository = contentItemRepository,
            initialSelectedTags = initialTags,
            initialSortType = initialSortType,
            onTagsChanged = { tags ->
                onTagsSelected(tags)
            },
            onSortTypeChanged = { sortType ->
                onSortTypeSelected(sortType)
            }
        )

        val initialQuery = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = initialTags,
            sortedBy = initialSortType
        )

        feedListComponent = FeedListComponentImpl(
            componentContext = componentContext,
            getContentUseCase = getContentUseCase,
            syncContentUseCase = syncContentUseCase,
            initialQuery = initialQuery,
            onItemClick = { itemId ->
                onListItemClick(itemId)
            }
        )
    }

    override fun onTagsSelected(tags: Tags) {
        val newState = _state.value.copy(selectedTags = tags)
        _state.value = newState
        updateFeedQuery()
    }

    override fun onSortTypeSelected(type: ContentItemsSortedType) {
        val newState = _state.value.copy(selectedSortType = type)
        _state.value = newState
        updateFeedQuery()
    }

    private fun updateFeedQuery() {
        val query = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = _state.value.selectedTags,
            sortedBy = _state.value.selectedSortType
        )
        if (feedListComponent is FeedListComponentImpl) {
            feedListComponent.updateQuery(query)
        }
    }

}