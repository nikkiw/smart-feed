package com.feature.feed.compose

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.feature.feed.R
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.list.FeedListComponent
import com.feature.feed.ui.openInternetSettings
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedListScreen(
    component: FeedListComponent,
    onChromeVisibilityChanged: (Boolean) -> Unit = {},
    topBarHeight: Dp = SmartFeedTopBarHeight,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val model by component.model.subscribeAsState()
    val pagingItems = component.pagingItems.collectAsLazyPagingItems()
    val initialScrollPosition = component.initialScrollPosition
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = initialScrollPosition.itemIndex,
            initialFirstVisibleItemScrollOffset = initialScrollPosition.itemOffsetPx,
        )
    val presentation by rememberFeedScreenState(
        model = model,
        pagingItems = pagingItems,
        context = context,
    )

    LaunchedEffect(component, context) {
        component.effects.collect { effect ->
            if (effect == FeedListComponent.Effect.OpenInternetSettings) {
                openInternetSettings(context)
            }
        }
    }

    LaunchedEffect(presentation) {
        if (presentation != FeedScreenState.Content) {
            onChromeVisibilityChanged(true)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) -> component.onScrollPositionChanged(index, offset) }
    }

    PullToRefreshBox(
        isRefreshing = model.refreshState is FeedListComponent.RefreshState.Refreshing,
        onRefresh = component::onRefresh,
        modifier =
        modifier
            .fillMaxSize()
            .testTag(SmartFeedUiTags.FEED_SCREEN),
    ) {
        Crossfade(
            targetState = presentation,
            modifier = Modifier.fillMaxSize(),
            label = "feed-list-state",
        ) { state ->
            FeedListStateContent(
                state = state,
                pagingItems = pagingItems,
                listState = listState,
                topBarHeight = topBarHeight,
                onChromeVisibilityChanged = onChromeVisibilityChanged,
                onEnableInternetClicked = component::onEnableInternetClicked,
                onRetry = component::onRetry,
                onListItemClick = component::onListItemClick,
            )
        }
    }
}

@Composable
private fun rememberFeedScreenState(
    model: FeedListComponent.Model,
    pagingItems: LazyPagingItems<ContentItemPreview>,
    context: Context,
) = remember(model, pagingItems.itemCount, pagingItems.loadState) {
    derivedStateOf {
        when {
            pagingItems.itemCount > 0 -> FeedScreenState.Content
            pagingItems.loadState.refresh is LoadState.Loading -> FeedScreenState.Loading
            !model.hasLocalContent && !model.isOnline -> FeedScreenState.Offline
            model.refreshState is FeedListComponent.RefreshState.Failed ->
                FeedScreenState.Error(
                    (model.refreshState as FeedListComponent.RefreshState.Failed).message,
                )
            pagingItems.loadState.refresh is LoadState.Error ->
                FeedScreenState.Error(
                    (pagingItems.loadState.refresh as LoadState.Error).error.message
                        ?: context.getString(R.string.error_loading_unknown),
                )
            else -> FeedScreenState.Empty
        }
    }
}

@Composable
private fun FeedListStateContent(
    state: FeedScreenState,
    pagingItems: LazyPagingItems<ContentItemPreview>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    topBarHeight: Dp,
    onChromeVisibilityChanged: (Boolean) -> Unit,
    onEnableInternetClicked: () -> Unit,
    onRetry: () -> Unit,
    onListItemClick: (ContentItemPreview) -> Unit,
) {
    when (state) {
        FeedScreenState.Loading -> {
            LoadingState(
                title = stringResource(R.string.feed_initial_loading_title),
                message = stringResource(R.string.feed_initial_loading_message),
            )
        }

        FeedScreenState.Content -> {
            FeedListContent(
                pagingItems = pagingItems,
                listState = listState,
                topBarHeight = topBarHeight,
                onChromeVisibilityChanged = onChromeVisibilityChanged,
                onListItemClick = onListItemClick,
            )
        }

        FeedScreenState.Offline -> {
            EmptyState(
                message = stringResource(R.string.error_loading_data),
                actionText = stringResource(R.string.enable_internet),
                onAction = onEnableInternetClicked,
            )
        }

        FeedScreenState.Empty -> {
            EmptyState(message = stringResource(R.string.feed_empty))
        }

        is FeedScreenState.Error -> {
            EmptyState(
                message = state.message,
                actionText = stringResource(R.string.retry),
                onAction = onRetry,
            )
        }
    }
}

@Composable
private fun FeedListContent(
    pagingItems: LazyPagingItems<ContentItemPreview>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    topBarHeight: Dp,
    onChromeVisibilityChanged: (Boolean) -> Unit,
    onListItemClick: (ContentItemPreview) -> Unit,
) {
    val spacing = SmartFeedThemeTokens.spacing
    val navigationBarPadding =
        WindowInsets.navigationBars
            .asPaddingValues()
            .calculateBottomPadding()

    ObserveLazyListChrome(
        state = listState,
        onVisibilityChange = onChromeVisibilityChanged,
    )

    LazyColumn(
        modifier =
        Modifier
            .fillMaxSize()
            .testTag(SmartFeedUiTags.FEED_LIST),
        state = listState,
        contentPadding =
        PaddingValues(
            start = spacing.large,
            top = topBarHeight + spacing.large,
            end = spacing.large,
            bottom = SmartFeedBottomBarHeight + navigationBarPadding + spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        items(
            count = pagingItems.itemCount,
            key = { index -> pagingItems.peek(index)?.id?.value ?: "feed-placeholder-$index" },
        ) { index ->
            pagingItems[index]?.let { preview ->
                PreviewCard(
                    preview = preview,
                    onClick = { onListItemClick(preview) },
                )
            }
        }

        feedAppendStateItem(
            append = pagingItems.loadState.append,
            onRetry = pagingItems::retry,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.feedAppendStateItem(
    append: LoadState,
    onRetry: () -> Unit,
) {
    when (append) {
        is LoadState.Loading -> item {
            FeedAppendLoadingItem()
        }

        is LoadState.Error -> item {
            InlineErrorCard(
                message = append.error.message ?: stringResource(R.string.error_loading_unknown),
                actionText = stringResource(R.string.retry),
                onAction = onRetry,
            )
        }

        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun FeedAppendLoadingItem() {
    val spacing = SmartFeedThemeTokens.spacing

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.small),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

internal sealed interface FeedScreenState {
    data object Loading : FeedScreenState

    data object Content : FeedScreenState

    data object Offline : FeedScreenState

    data object Empty : FeedScreenState

    data class Error(val message: String) : FeedScreenState
}
