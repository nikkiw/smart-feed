package com.feature.feed.master

import com.arkivanov.decompose.value.Value
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemsSortedType
import com.feature.feed.filter.FilterSortComponent
import com.feature.feed.list.FeedListComponent

interface FeedMasterComponent {

    val filterSortComponent: FilterSortComponent
    val feedListComponent: FeedListComponent

    /**
     * The status of the current filters and sorting.
     */
    val state: Value<State>

    data class State(
        val selectedTags: Tags = Tags(),
        val selectedSortType: ContentItemsSortedType = ContentItemsSortedType.ByDateNewestFirst
    )

    /**
     * Called when the user changes the filter by tags.
     */
    fun onTagsSelected(tags: Tags)

    /**
     * Called when the user changes the sorting type.
     */
    fun onSortTypeSelected(type: ContentItemsSortedType)

}