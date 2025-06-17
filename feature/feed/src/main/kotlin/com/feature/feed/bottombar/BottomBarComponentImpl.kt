package com.feature.feed.bottombar

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.feature.feed.bottombar.model.BottomBarState


class BottomBarComponentImpl(
    private val componentContext: ComponentContext,
    internal val onTabBarChanged: (BottomBarState) -> Unit
) : BottomBarComponent, ComponentContext by componentContext {

    private val _state = MutableValue<BottomBarState>(BottomBarState.List)
    override val state: Value<BottomBarState> = _state


    override fun onClickTabBar(newState: BottomBarState) {
        onTabBarChanged(newState)
    }

}