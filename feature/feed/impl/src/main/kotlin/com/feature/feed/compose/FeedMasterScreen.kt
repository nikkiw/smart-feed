package com.feature.feed.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.feature.feed.R
import com.feature.feed.domain.repository.ContentItemsSortedType
import com.feature.feed.filter.FilterSortComponent
import com.feature.feed.master.FeedMasterComponent

@Composable
internal fun FeedMasterScreen(
    component: FeedMasterComponent,
    onChromeVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var topBarVisible by remember { mutableStateOf(true) }
    // Measure the real toolbar height so the list top padding stays stable —
    // the toolbar floats *over* the list, list size never changes on show/hide.
    var topBarHeightPx by remember { mutableIntStateOf(0) }
    val topBarHeightDp = with(LocalDensity.current) { topBarHeightPx.toDp() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        FeedListScreen(
            component = component.feedListComponent,
            topBarHeight = topBarHeightDp,
            onChromeVisibilityChanged = { visible ->
                topBarVisible = visible
                onChromeVisibilityChanged(visible)
            },
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = topBarVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .onSizeChanged { topBarHeightPx = it.height },
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
        ) {
            FilterSortSection(
                component = component.filterSortComponent,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun FilterSortSection(component: FilterSortComponent, modifier: Modifier = Modifier) {
    val state by component.state.subscribeAsState()
    val spacing = SmartFeedThemeTokens.spacing
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier =
        modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = spacing.medium, vertical = spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        if (state.availableTags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
                items(
                    items = state.availableTags,
                    key = { it },
                ) { tag ->
                    FilterChip(
                        selected = tag in state.selectedTags.value,
                        onClick = { component.onTagClicked(tag) },
                        label = { Text(tag, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.sorted_by),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Box {
                OutlinedButton(
                    onClick = { sortMenuExpanded = true },
                    contentPadding =
                    PaddingValues(
                        horizontal = spacing.medium,
                        vertical = spacing.small,
                    ),
                ) {
                    Text(
                        text = stringResource(state.selectedSortType.labelRes()),
                        maxLines = 1,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }

                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { sortMenuExpanded = false },
                ) {
                    state.availableSortTypes.forEach { sortType ->
                        DropdownMenuItem(
                            text = { Text(stringResource(sortType.labelRes())) },
                            onClick = {
                                sortMenuExpanded = false
                                component.onSortTypeSelected(sortType)
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun ContentItemsSortedType.labelRes(): Int = when (this) {
    ContentItemsSortedType.ByNameAsc -> R.string.sort_name_asc
    ContentItemsSortedType.ByNameDesc -> R.string.sort_name_desc
    ContentItemsSortedType.ByDateNewestFirst -> R.string.sort_date_newest
    ContentItemsSortedType.ByDateOldestFirst -> R.string.sort_date_oldest
}
