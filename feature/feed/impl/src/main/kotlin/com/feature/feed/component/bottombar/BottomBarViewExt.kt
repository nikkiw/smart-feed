package com.feature.feed.component.bottombar

import android.view.View
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.feature.feed.R
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.model.BottomBarState
import com.google.android.material.bottomnavigation.BottomNavigationView

@OptIn(ExperimentalDecomposeApi::class)
@Suppress("FunctionName")
fun ViewContext.BottomBarView(component: BottomBarComponent): View {
    val view = layoutInflater.inflate(R.layout.bottom_bar_view, parent, false)

    val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNavigation)

    component.state.subscribe { state ->
        bottomNav.selectedItemId =
            when (state) {
                BottomBarState.List -> R.id.action_list
                BottomBarState.Recommendation -> R.id.action_recommendation
            }
    }

    bottomNav.setOnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.action_list -> {
                component.onClickTabBar(BottomBarState.List)
                true
            }

            R.id.action_recommendation -> {
                component.onClickTabBar(BottomBarState.Recommendation)
                true
            }

            else -> false
        }
    }

    return view
}
