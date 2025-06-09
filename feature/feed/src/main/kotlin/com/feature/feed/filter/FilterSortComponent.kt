package com.feature.feed.filter

import com.arkivanov.decompose.value.Value
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemsSortedType

/**
 * Component for filtering by tags and selecting sorting.
 */
interface FilterSortComponent {

    /**
     * UI state of filtering and sorting.
     */
    val state: Value<State>

    data class State(
        val availableTags: List<String> = emptyList(),
        val selectedTags: Tags = Tags(),
        val availableSortTypes: List<ContentItemsSortedType> = ContentItemsSortedType.entries.toList(),
        val selectedSortType: ContentItemsSortedType = ContentItemsSortedType.ByDateNewestFirst
    )

    /**
     * The user has selected/deselected the tag.
     */
    fun onTagClicked(tag: String)

    /**
     * The user has changed the sort type.
     */
    fun onSortTypeSelected(type: ContentItemsSortedType)
}