package com.feature.feed.component.master

import android.view.View
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.child
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.feature.feed.R
import com.feature.feed.component.filter.FilterSortView
import com.feature.feed.component.list.ui.FeedListView
import com.feature.feed.master.FeedMasterComponent

/**
 * ViewContext-extension to display the entire feed: filter + list.
 */
@OptIn(ExperimentalDecomposeApi::class)
@Suppress("FunctionName")
fun ViewContext.FeedMasterView(component: FeedMasterComponent): View {
    val layout = layoutInflater.inflate(R.layout.feed_master_view, parent, false)

    // Контейнеры во View
    child(layout.findViewById(R.id.filterSortContainer)) {
        FilterSortView(component.filterSortComponent)
    }

    child(layout.findViewById(R.id.feedListContainer)) {
        FeedListView(component.feedListComponent)
    }

    return layout
}
