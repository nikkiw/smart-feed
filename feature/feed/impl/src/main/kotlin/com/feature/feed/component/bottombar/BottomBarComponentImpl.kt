package com.feature.feed.component.bottombar

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.model.BottomBarState

class BottomBarComponentImpl(
    componentContext: ComponentContext,
    private val onTabBarChanged: (BottomBarState) -> Unit,
) : BottomBarComponent, ComponentContext by componentContext {
    private val stateHolder =
        instanceKeeper.getOrCreate(STATE_HOLDER_KEY) {
            StateHolder(BottomBarState.List)
        }
    private val _state = MutableValue(stateHolder.state)
    override val state: Value<BottomBarState> = _state

    override fun onClickTabBar(newState: BottomBarState) {
        if (_state.value == newState) return

        stateHolder.state = newState
        _state.value = newState
        onTabBarChanged(newState)
    }

    private class StateHolder(
        var state: BottomBarState,
    ) : InstanceKeeper.Instance

    private companion object {
        const val STATE_HOLDER_KEY = "BottomBarStateHolder"
    }
}
