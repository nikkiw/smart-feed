package com.feature.feed.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.feature.feed.R
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.model.BottomBarState

@Composable
internal fun FeedBottomBar(component: BottomBarComponent, modifier: Modifier = Modifier) {
    val selected by component.state.subscribeAsState()

    NavigationBar(modifier = modifier.fillMaxWidth()) {
        NavigationBarItem(
            modifier = Modifier.testTag(SmartFeedUiTags.TAB_FEED),
            selected = selected == BottomBarState.List,
            onClick = { component.onClickTabBar(BottomBarState.List) },
            icon = { Icon(Icons.Rounded.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.bottom_list)) },
        )
        NavigationBarItem(
            modifier = Modifier.testTag(SmartFeedUiTags.TAB_RECOMMENDATIONS),
            selected = selected == BottomBarState.Recommendation,
            onClick = { component.onClickTabBar(BottomBarState.Recommendation) },
            icon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
            label = { Text(stringResource(R.string.bottom_recommendation)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FeedBottomBarPreview() {
    SmartFeedTheme {
        FeedBottomBar(component = PreviewBottomBarComponent())
    }
}

private class PreviewBottomBarComponent : BottomBarComponent {
    override val state: Value<BottomBarState> = MutableValue(BottomBarState.List)

    override fun onClickTabBar(newState: BottomBarState) = Unit
}
