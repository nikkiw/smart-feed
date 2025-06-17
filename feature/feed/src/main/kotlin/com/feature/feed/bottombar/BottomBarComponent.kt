package com.feature.feed.bottombar

import com.arkivanov.decompose.value.Value
import com.feature.feed.bottombar.model.BottomBarState

interface BottomBarComponent {

    val state: Value<BottomBarState>

    fun onClickTabBar(newState: BottomBarState)
}