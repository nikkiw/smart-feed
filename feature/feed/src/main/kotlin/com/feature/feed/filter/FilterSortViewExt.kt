package com.feature.feed.filter

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.context
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.feature.feed.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/**
 * An extension function for ViewContext that creates a FilterSortView and associates it with a component.
 */
@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.FilterSortView(component: FilterSortComponent): View {

    val view = layoutInflater.inflate(R.layout.filter_sort, parent, false)

    val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupTags)
    val spinner = view.findViewById<Spinner>(R.id.spinnerSortType)

    component.state.subscribe { state ->
        // Теги
        chipGroup.removeAllViews()
        for (tag in state.availableTags) {
            val chip = Chip(context)
            chip.text = tag
            chip.isCheckable = true
            chip.isChecked = state.selectedTags.value.contains(tag)
            chip.setOnClickListener { component.onTagClicked(tag) }
            chipGroup.addView(chip)
        }

        val items = state.availableSortTypes
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            items.map { it.name }
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinner.adapter = adapter
        spinner.setSelection(items.indexOf(state.selectedSortType))
    }

    spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: android.widget.AdapterView<*>,
            view: View?,
            position: Int,
            id: Long
        ) {
            component.onSortTypeSelected(
                component.state.value.availableSortTypes[position]
            )
        }

        override fun onNothingSelected(parent: android.widget.AdapterView<*>) = Unit
    }

    return view
}