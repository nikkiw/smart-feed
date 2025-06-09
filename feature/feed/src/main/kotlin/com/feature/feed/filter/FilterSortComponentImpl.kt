package com.feature.feed.filter

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.ContentItemsSortedType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * FilterSortComponent implementation from ComponentContext.
 */
class FilterSortComponentImpl(
    componentContext: ComponentContext,
    private val contentItemRepository: ContentItemRepository,
    initialSelectedTags: Tags = Tags(),
    initialSortType: ContentItemsSortedType = ContentItemsSortedType.ByDateNewestFirst,
    private val onTagsChanged: (Tags) -> Unit,
    private val onSortTypeChanged: (ContentItemsSortedType) -> Unit,
) : FilterSortComponent, ComponentContext by componentContext {

    private val scope = coroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _state = MutableValue(
        FilterSortComponent.State(
            availableTags =  emptyList(),
            selectedTags = initialSelectedTags,
            availableSortTypes = ContentItemsSortedType.entries.toList(),
            selectedSortType = initialSortType
        )
    )
    override val state: Value<FilterSortComponent.State> = _state

    init {
        // Subscribe to flowAllTags() and update availableTags in _state with each new list of Tags
        scope.launch {
            contentItemRepository.flowAllTags().collect { tagsObj ->
                // Обновляем состояние, сохраняя остальные поля как есть
                _state.value = _state.value.copy(
                    availableTags =  tagsObj.value
                )
            }
        }
    }


    override fun onTagClicked(tag: String) {
        val current = _state.value
        val tags = current.selectedTags.value.toMutableList()
        if (tags.contains(tag)) {
            tags.remove(tag)
        } else {
            tags.add(tag)
        }
        val newTags = Tags(tags)
        _state.value = current.copy(selectedTags = newTags)
        onTagsChanged(newTags)
    }

    override fun onSortTypeSelected(type: ContentItemsSortedType) {
        val current = _state.value
        if (current.selectedSortType != type) {
            _state.value = current.copy(selectedSortType = type)
            onSortTypeChanged(type)
        }
    }
}