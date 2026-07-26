package com.feature.feed.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.core.content.model.ContentId
import com.core.content.model.ImageUrl
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt
import com.feature.feed.R
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.recommendation.RecommendationListComponent
import com.feature.feed.ui.openInternetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow

@Composable
internal fun RecommendationScreen(
    component: RecommendationListComponent,
    onChromeVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val model by component.model.subscribeAsState()
    val initialScrollPosition = component.initialScrollPosition
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = initialScrollPosition.itemIndex,
            initialFirstVisibleItemScrollOffset = initialScrollPosition.itemOffsetPx,
        )
    val state = rememberRecommendationScreenState(model)

    LaunchedEffect(component, context) {
        component.effects.collect { effect ->
            if (effect == RecommendationListComponent.Effect.OpenInternetSettings) {
                openInternetSettings(context)
            }
        }
    }

    LaunchedEffect(state) {
        if (state != RecommendationScreenState.Content) {
            onChromeVisibilityChanged(true)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) -> component.onScrollPositionChanged(index, offset) }
    }

    Crossfade(
        targetState = state,
        modifier =
        modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag(SmartFeedUiTags.RECOMMENDATION_SCREEN),
        label = "recommendation-screen-state",
    ) { current ->
        RecommendationScreenStateContent(
            state = current,
            model = model,
            listState = listState,
            onChromeVisibilityChanged = onChromeVisibilityChanged,
            onEnableInternetClicked = component::onEnableInternetClicked,
            onRetry = component::onRetry,
            onListItemClick = component::onListItemClick,
        )
    }
}

@Composable
private fun rememberRecommendationScreenState(model: RecommendationListComponent.Model): RecommendationScreenState =
    remember(model) {
        when {
            model.loadState is RecommendationListComponent.LoadState.Loading && model.items.isEmpty() ->
                RecommendationScreenState.Loading
            model.items.isNotEmpty() -> RecommendationScreenState.Content
            !model.hasLocalContent && !model.isOnline -> RecommendationScreenState.Offline
            model.loadState is RecommendationListComponent.LoadState.Failed ->
                RecommendationScreenState.Error(
                    (model.loadState as RecommendationListComponent.LoadState.Failed).message,
                )
            else -> RecommendationScreenState.Empty
        }
    }

@Composable
private fun RecommendationScreenStateContent(
    state: RecommendationScreenState,
    model: RecommendationListComponent.Model,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onChromeVisibilityChanged: (Boolean) -> Unit,
    onEnableInternetClicked: () -> Unit,
    onRetry: () -> Unit,
    onListItemClick: (ContentItemPreview) -> Unit,
) {
    when (state) {
        RecommendationScreenState.Loading -> {
            LoadingState(
                title = stringResource(R.string.bottom_recommendation),
                message = stringResource(R.string.feed_initial_loading_message),
            )
        }

        RecommendationScreenState.Content -> {
            RecommendationListContent(
                items = model.items,
                listState = listState,
                onChromeVisibilityChanged = onChromeVisibilityChanged,
                onListItemClick = onListItemClick,
            )
        }

        RecommendationScreenState.Offline -> {
            EmptyState(
                message = stringResource(R.string.error_loading_data),
                actionText = stringResource(R.string.enable_internet),
                onAction = onEnableInternetClicked,
            )
        }

        RecommendationScreenState.Empty -> {
            EmptyState(message = stringResource(R.string.recommendations_empty))
        }

        is RecommendationScreenState.Error -> {
            EmptyState(
                message = state.message,
                actionText = stringResource(R.string.retry),
                onAction = onRetry,
            )
        }
    }
}

@Composable
private fun RecommendationListContent(
    items: List<ContentItemPreview>,
    listState: androidx.compose.foundation.lazy.LazyListState,
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
            .testTag(SmartFeedUiTags.RECOMMENDATION_LIST),
        state = listState,
        contentPadding =
        PaddingValues(
            start = spacing.large,
            top = spacing.large,
            end = spacing.large,
            bottom = SmartFeedBottomBarHeight + navigationBarPadding + spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        items(
            items = items,
            key = { it.id.value },
        ) { preview ->
            PreviewCard(
                preview = preview,
                onClick = { onListItemClick(preview) },
            )
        }
    }
}

internal sealed interface RecommendationScreenState {
    data object Loading : RecommendationScreenState

    data object Content : RecommendationScreenState

    data object Offline : RecommendationScreenState

    data object Empty : RecommendationScreenState

    data class Error(val message: String) : RecommendationScreenState
}

@Preview(showBackground = true)
@Composable
private fun RecommendationScreenPreview() {
    SmartFeedTheme {
        RecommendationScreen(
            component =
            PreviewRecommendationListComponent(
                RecommendationListComponent.Model(
                    items =
                    listOf(
                        ContentItemPreview.ArticlePreview(
                            id = ContentId("preview-recommendation"),
                            updatedAt = UpdatedAt.now(),
                            mainImageUrl = ImageUrl(""),
                            tags = Tags(listOf("Compose", "Portfolio")),
                            title = Title("Recommendation preview"),
                            short = ShortDescription("Screen-level preview for the recommendation list."),
                        ),
                    ),
                    isOnline = true,
                    hasLocalContent = true,
                    loadState = RecommendationListComponent.LoadState.Idle,
                ),
            ),
        )
    }
}

private class PreviewRecommendationListComponent(
    initialModel: RecommendationListComponent.Model,
) : RecommendationListComponent {
    override val model: Value<RecommendationListComponent.Model> = MutableValue(initialModel)
    override val initialScrollPosition: RecommendationListComponent.ScrollPosition =
        RecommendationListComponent.ScrollPosition()
    override val effects: Flow<RecommendationListComponent.Effect> = emptyFlow()

    override fun onRetry() = Unit

    override fun onEnableInternetClicked() = Unit

    override fun onListItemClick(item: ContentItemPreview) = Unit

    override fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int) = Unit
}
