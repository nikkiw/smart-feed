package com.feature.feed.compose

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow

private const val CHROME_SCROLL_THRESHOLD_PX = 24

@Composable
internal fun ObserveLazyListChrome(state: LazyListState, onVisibilityChange: (Boolean) -> Unit) {
    val currentOnVisibilityChange by rememberUpdatedState(onVisibilityChange)

    LaunchedEffect(state) {
        var anchorIndex = state.firstVisibleItemIndex
        var anchorOffset = state.firstVisibleItemScrollOffset
        var previousVisible = true
        currentOnVisibilityChange(true)

        snapshotFlow {
            Triple(
                state.isScrollInProgress,
                state.firstVisibleItemIndex,
                state.firstVisibleItemScrollOffset,
            )
        }.collect { (isScrollInProgress, index, offset) ->
            if (!isScrollInProgress) {
                anchorIndex = index
                anchorOffset = offset
                return@collect
            }

            val nextVisible =
                when {
                    index == 0 && offset == 0 -> true
                    index > anchorIndex -> false
                    index < anchorIndex -> true
                    offset >= anchorOffset + CHROME_SCROLL_THRESHOLD_PX -> false
                    offset <= anchorOffset - CHROME_SCROLL_THRESHOLD_PX -> true
                    else -> previousVisible
                }

            if (nextVisible != previousVisible) {
                previousVisible = nextVisible
                currentOnVisibilityChange(nextVisible)
                anchorIndex = index
                anchorOffset = offset
            }
        }
    }
}

@Composable
internal fun ObserveScrollChrome(state: ScrollState, onVisibilityChange: (Boolean) -> Unit) {
    val currentOnVisibilityChange by rememberUpdatedState(onVisibilityChange)

    LaunchedEffect(state) {
        var anchorValue = state.value
        var previousVisible = true
        currentOnVisibilityChange(true)

        snapshotFlow { state.isScrollInProgress to state.value }.collect { (isScrollInProgress, value) ->
            if (!isScrollInProgress) {
                anchorValue = value
                return@collect
            }

            val nextVisible =
                when {
                    value == 0 -> true
                    value >= anchorValue + CHROME_SCROLL_THRESHOLD_PX -> false
                    value <= anchorValue - CHROME_SCROLL_THRESHOLD_PX -> true
                    else -> previousVisible
                }

            if (nextVisible != previousVisible) {
                previousVisible = nextVisible
                currentOnVisibilityChange(nextVisible)
                anchorValue = value
            }
        }
    }
}
