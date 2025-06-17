package com.feature.feed.bottombar.model

sealed interface BottomBarState {

    data object List : BottomBarState

    data object Recommendation : BottomBarState

}
