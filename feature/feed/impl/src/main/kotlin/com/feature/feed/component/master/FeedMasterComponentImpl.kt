package com.feature.feed.component.master

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.core.content.model.ContentType
import com.core.content.model.Tags
import com.feature.feed.component.filter.FilterSortComponentImpl
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.repository.ContentItemsSortedType
import com.feature.feed.domain.repository.Query
import com.feature.feed.filter.FilterSortComponent
import com.feature.feed.list.FeedListComponent
import com.feature.feed.master.FeedMasterComponent

/**
 * FeedRootComponent implementation.
 * Encapsulates the Filter Sort Component, Feed List Component, and the Article Item Component factory.
 */
@Suppress("LongParameterList")
class FeedMasterComponentImpl(
    componentContext: ComponentContext,
    private val contentItemRepository: ContentItemRepository,
    private val feedListComponentFactory: FeedListComponent.Factory,
    onListItemClick: (ContentItemPreview) -> Unit,
    initialSortType: ContentItemsSortedType = ContentItemsSortedType.ByDateNewestFirst,
    initialTags: Tags = Tags(),
) : FeedMasterComponent, ComponentContext by componentContext {
    private val stateHolder =
        instanceKeeper.getOrCreate(STATE_HOLDER_KEY) {
            StateHolder(
                FeedMasterComponent.State(
                    selectedTags = initialTags,
                    selectedSortType = initialSortType,
                ),
            )
        }
    private val _state =
        MutableValue(stateHolder.state)
    override val state: Value<FeedMasterComponent.State> = _state

    // We keep a link to the Feed List Component to update the query when changing the filter/sorting.
    override val feedListComponent: FeedListComponent

    override val filterSortComponent: FilterSortComponent

    init {
        filterSortComponent =
            FilterSortComponentImpl(
                componentContext = childContext(key = "filterSort"),
                contentItemRepository = contentItemRepository,
                initialSelectedTags = _state.value.selectedTags,
                initialSortType = _state.value.selectedSortType,
                onTagsChanged = { tags ->
                    onTagsSelected(tags)
                },
                onSortTypeChanged = { sortType ->
                    onSortTypeSelected(sortType)
                },
            )

        val initialQuery =
            Query(
                types = listOf(ContentType.ARTICLE),
                tags = _state.value.selectedTags,
                sortedBy = _state.value.selectedSortType,
            )

        feedListComponent =
            feedListComponentFactory(
                componentContext = childContext(key = "feedList"),
                initialQuery = initialQuery,
                onItemClick = { preview ->
                    onListItemClick(preview)
                },
            )
    }

    override fun onTagsSelected(tags: Tags) {
        val newState = _state.value.copy(selectedTags = tags)
        stateHolder.state = newState
        _state.value = newState
        updateFeedQuery()
    }

    override fun onSortTypeSelected(type: ContentItemsSortedType) {
        val newState = _state.value.copy(selectedSortType = type)
        stateHolder.state = newState
        _state.value = newState
        updateFeedQuery()
    }

    private fun updateFeedQuery() {
        val query =
            Query(
                types = listOf(ContentType.ARTICLE),
                tags = _state.value.selectedTags,
                sortedBy = _state.value.selectedSortType,
            )
        feedListComponent.updateQuery(query)
    }

    private class StateHolder(
        var state: FeedMasterComponent.State,
    ) : InstanceKeeper.Instance

    private companion object {
        const val STATE_HOLDER_KEY = "FeedMasterStateHolder"
    }
}
