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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.feature.feed.R
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.component.root.toArticleRoutePreview
import com.feature.feed.domain.model.ContentItem
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.root.ArticleRoutePreview
import kotlinx.coroutines.flow.distinctUntilChanged

private const val ARTICLE_READ_PROGRESS_BEFORE_BODY = 0.1f
private const val ARTICLE_READ_PROGRESS_AFTER_BODY = 0.9f
private const val RELATED_CONTENT_START_INDEX = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ArticleScreen(
    component: ArticleItemComponent,
    routePreview: ArticleRoutePreview? = null,
    onChromeVisibilityChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val model by component.model.subscribeAsState()
    var topBarVisible by remember { mutableStateOf(true) }
    val loadedArticle =
        ((model.contentState as? ArticleItemComponent.ContentState.Content)?.contentItem as? ContentItem.Article)
    val previewSnapshot = loadedArticle?.toArticleRoutePreview() ?: routePreview

    LaunchedEffect(model.contentState) {
        if (previewSnapshot == null) {
            topBarVisible = true
            onChromeVisibilityChanged(true)
        }
    }

    Box(
        modifier =
        modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        when {
            previewSnapshot != null -> {
                key(component.itemId.value) {
                    val initialScrollPosition = component.initialScrollPosition
                    ArticleContent(
                        articlePreview = previewSnapshot,
                        article = loadedArticle,
                        loadFailedMessage =
                        (model.contentState as? ArticleItemComponent.ContentState.Failed)?.message,
                        initialScrollPosition = initialScrollPosition,
                        recommendationsComponent = component.articleRecommendationsComponent,
                        onReadProgressChanged = component::onReadProgressChanged,
                        onScrollPositionChanged = component::onScrollPositionChanged,
                        onRetry = component::onRetry,
                        onChromeVisibilityChanged = { visible ->
                            topBarVisible = visible
                            onChromeVisibilityChanged(visible)
                        },
                    )
                }
            }

            model.contentState is ArticleItemComponent.ContentState.Failed -> {
                EmptyState(
                    message = (model.contentState as ArticleItemComponent.ContentState.Failed).message,
                    actionText = stringResource(R.string.retry),
                    onAction = component::onRetry,
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag(SmartFeedUiTags.ARTICLE_SCREEN),
                )
            }

            else -> {
                LoadingState(
                    title = stringResource(R.string.article_loading_title),
                    message = stringResource(R.string.feed_initial_loading_message),
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag(SmartFeedUiTags.ARTICLE_SCREEN),
                )
            }
        }

        val title =
            previewSnapshot
                ?.title
                .orEmpty()

        AnimatedVisibility(
            visible = topBarVisible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut(),
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = component::onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.article_back),
                        )
                    }
                },
                colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    }
}

@Composable
private fun ArticleContent(
    articlePreview: ArticleRoutePreview,
    article: ContentItem.Article?,
    loadFailedMessage: String?,
    initialScrollPosition: ArticleItemComponent.ScrollPosition,
    recommendationsComponent: ArticleRecommendationsComponent,
    onReadProgressChanged: (Float) -> Unit,
    onScrollPositionChanged: (Int, Int) -> Unit,
    onRetry: () -> Unit,
    onChromeVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState =
        rememberLazyListState(
            initialFirstVisibleItemIndex = initialScrollPosition.itemIndex,
            initialFirstVisibleItemScrollOffset = initialScrollPosition.itemOffsetPx,
        )
    val recommendationsModel by recommendationsComponent.model.subscribeAsState()
    val spacing = SmartFeedThemeTokens.spacing
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(listState) {
        snapshotFlow { calculateArticleReadProgress(listState = listState, articleId = articlePreview.id) }
            .distinctUntilChanged()
            .collect(onReadProgressChanged)
    }

    ObserveLazyListChrome(
        state = listState,
        onVisibilityChange = onChromeVisibilityChanged,
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .distinctUntilChanged()
            .collect { (index, offset) -> onScrollPositionChanged(index, offset) }
    }

    LazyColumn(
        modifier =
        modifier
            .fillMaxSize()
            .testTag(SmartFeedUiTags.ARTICLE_CONTENT),
        state = listState,
        contentPadding =
        PaddingValues(
            start = spacing.large,
            top = SmartFeedTopBarHeight + spacing.large,
            end = spacing.large,
            bottom = SmartFeedBottomBarHeight + navigationBarPadding + spacing.large,
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.large),
    ) {
        item(key = heroItemKey(articlePreview.id)) {
            ArticleHeroCard(articlePreview = articlePreview)
        }
        item(key = markdownItemKey(articlePreview.id)) {
            when {
                article != null -> ArticleMarkdownContent(content = article.content.value)
                loadFailedMessage != null ->
                    EmptyState(
                        message = loadFailedMessage,
                        actionText = stringResource(R.string.retry),
                        onAction = onRetry,
                    )
                else -> ArticleBodyLoadingPlaceholder()
            }
        }
        item(key = dividerItemKey(articlePreview.id)) {
            HorizontalDivider()
        }
        relatedArticlesItems(
            state = recommendationsModel.state,
            onRetry = recommendationsComponent::onRetry,
            onArticleClick = recommendationsComponent::onListItemClick,
        )
    }
}

private fun calculateArticleReadProgress(
    listState: androidx.compose.foundation.lazy.LazyListState,
    articleId: String,
): Float {
    val layoutInfo = listState.layoutInfo
    val viewportStart = layoutInfo.viewportStartOffset
    val viewportEnd = layoutInfo.viewportEndOffset
    val viewportHeight = (viewportEnd - viewportStart).coerceAtLeast(1)
    val visibleItems = layoutInfo.visibleItemsInfo
    val bodyItem = visibleItems.firstOrNull { it.key == markdownItemKey(articleId) }

    return when {
        !listState.canScrollBackward -> 0f
        !listState.canScrollForward -> 1f
        bodyItem != null -> {
            val bodyScrollablePx = (bodyItem.size - viewportHeight).coerceAtLeast(0)
            val bodyProgress =
                if (bodyScrollablePx == 0) {
                    val visiblePx =
                        (
                            minOf(bodyItem.offset + bodyItem.size, viewportEnd) -
                                maxOf(bodyItem.offset, viewportStart)
                            ).coerceAtLeast(0)
                    if (bodyItem.size == 0) {
                        0f
                    } else {
                        visiblePx.toFloat() / bodyItem.size.toFloat()
                    }
                } else {
                    (-bodyItem.offset).coerceIn(0, bodyScrollablePx).toFloat() /
                        bodyScrollablePx.toFloat()
                }

            (
                ARTICLE_READ_PROGRESS_BEFORE_BODY +
                    (ARTICLE_READ_PROGRESS_AFTER_BODY - ARTICLE_READ_PROGRESS_BEFORE_BODY) * bodyProgress
                ).coerceIn(0f, ARTICLE_READ_PROGRESS_AFTER_BODY)
        }
        listState.firstVisibleItemIndex <= 0 -> 0f
        listState.firstVisibleItemIndex <= 1 -> ARTICLE_READ_PROGRESS_BEFORE_BODY
        listState.firstVisibleItemIndex <= 2 -> ARTICLE_READ_PROGRESS_AFTER_BODY
        else -> {
            val lastVisibleIndex = visibleItems.lastOrNull()?.index ?: listState.firstVisibleItemIndex
            val relatedItemsDenominator =
                (layoutInfo.totalItemsCount - 1 - RELATED_CONTENT_START_INDEX).coerceAtLeast(1)
            val relatedProgress =
                (
                    (lastVisibleIndex - RELATED_CONTENT_START_INDEX).toFloat() /
                        relatedItemsDenominator.toFloat()
                    ).coerceIn(0f, 1f)

            (
                ARTICLE_READ_PROGRESS_AFTER_BODY +
                    (1f - ARTICLE_READ_PROGRESS_AFTER_BODY) * relatedProgress
                ).coerceIn(ARTICLE_READ_PROGRESS_AFTER_BODY, 1f)
        }
    }
}

private fun heroItemKey(articleId: String): String = "hero_$articleId"

private fun markdownItemKey(articleId: String): String = "markdown_$articleId"

private fun dividerItemKey(articleId: String): String = "divider_$articleId"

@Composable
private fun ArticleHeroCard(articlePreview: ArticleRoutePreview) {
    val spacing = SmartFeedThemeTokens.spacing

    Card(
        modifier =
        Modifier
            .fillMaxWidth()
            .smartFeedSharedBounds(
                sharedCardKey(articlePreview.id),
                articlePreview.id,
                clipShape = MaterialTheme.shapes.large,
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = spacing.xSmall),
    ) {
        Column(
            modifier = Modifier.padding(all = spacing.large),
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            AsyncImage(
                model = articlePreview.mainImageUrl.ifBlank { null },
                contentDescription =
                stringResource(
                    R.string.article_main_image_content_description,
                    articlePreview.title,
                ),
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.large)
                    .smartFeedSharedBounds(
                        sharedImageKey(articlePreview.id),
                        articlePreview.id,
                        clipShape = MaterialTheme.shapes.large,
                    ),
                contentScale = ContentScale.Crop,
            )
            Text(
                text = articlePreview.title,
                modifier =
                Modifier
                    .smartFeedSharedBounds(sharedTitleKey(articlePreview.id), articlePreview.id)
                    .semantics { heading() },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            MetadataRow(articlePreview = articlePreview)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MetadataRow(articlePreview: ArticleRoutePreview) {
    val spacing = SmartFeedThemeTokens.spacing
    val formattedUpdatedAt =
        rememberFormattedUpdatedAt(
            com.core.content.model.UpdatedAt(articlePreview.updatedAtEpochMillis),
        )
    val tagGroupDescription = stringResource(R.string.article_tags)

    Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
        Text(
            text = formattedUpdatedAt,
            modifier =
            Modifier.smartFeedSharedBounds(
                sharedUpdatedAtKey(articlePreview.id),
                articlePreview.id,
            ),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.semantics { contentDescription = tagGroupDescription },
            horizontalArrangement = Arrangement.spacedBy(spacing.small),
            verticalArrangement = Arrangement.spacedBy(spacing.small),
        ) {
            articlePreview.tags.forEach { tag ->
                TagChip(
                    tag = tag,
                    modifier =
                    Modifier.smartFeedSharedBounds(
                        sharedTagKey(articlePreview.id, tag),
                        articlePreview.id,
                    ),
                )
            }
        }
    }
}

private fun LazyListScope.relatedArticlesItems(
    state: ArticleRecommendationsComponent.State,
    onRetry: () -> Unit,
    onArticleClick: (ContentItemPreview) -> Unit,
) {
    item(key = "related_header") {
        Text(
            text = stringResource(R.string.recommended_reading),
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }

    when (state) {
        ArticleRecommendationsComponent.State.Loading -> {
            item(key = "related_loading") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        is ArticleRecommendationsComponent.State.Content -> {
            items(
                items = state.items,
                key = { preview -> "related_${preview.id.value}" },
            ) { preview ->
                RelatedPreviewRowCard(
                    preview = preview,
                    onClick = { onArticleClick(preview) },
                )
            }
        }

        ArticleRecommendationsComponent.State.Empty -> {
            item(key = "related_empty") {
                EmptyState(message = stringResource(R.string.related_articles_empty))
            }
        }

        is ArticleRecommendationsComponent.State.Failed -> {
            item(key = "related_error") {
                EmptyState(
                    message = state.message,
                    actionText = stringResource(R.string.retry),
                    onAction = onRetry,
                )
            }
        }
    }
}

@Composable
private fun ArticleBodyLoadingPlaceholder() {
    val spacing = SmartFeedThemeTokens.spacing
    val placeholderColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.medium),
    ) {
        repeat(6) { index ->
            Box(
                modifier =
                Modifier
                    .fillMaxWidth(
                        when (index % 3) {
                            0 -> 1f
                            1 -> 0.92f
                            else -> 0.78f
                        },
                    )
                    .height(if (index == 0) 28.dp else 18.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(placeholderColor),
            )
        }
    }
}

@Composable
private fun RelatedPreviewRowCard(preview: ContentItemPreview, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = SmartFeedThemeTokens.spacing
    val sharedTransitionContentId = LocalSharedTransitionContentId.current
    val model = preview.toRelatedPreviewModel()

    Card(
        modifier =
        modifier
            .fillMaxWidth()
            .smartFeedSharedBounds(
                sharedCardKey(model.id),
                model.id,
                clipShape = MaterialTheme.shapes.medium,
            )
            .testTag("related_preview_row"),
        onClick = {
            sharedTransitionContentId?.value = model.id
            onClick()
        },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = spacing.xSmall),
    ) {
        Row(
            modifier = Modifier.padding(all = spacing.medium),
            horizontalArrangement = Arrangement.spacedBy(spacing.medium),
        ) {
            AsyncImage(
                model = model.imageUrl.ifBlank { null },
                contentDescription = null,
                modifier =
                Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
                    .smartFeedSharedBounds(
                        sharedImageKey(model.id),
                        model.id,
                        clipShape = MaterialTheme.shapes.small,
                    ),
                contentScale = ContentScale.Crop,
            )

            RelatedPreviewRowDetails(
                modifier = Modifier.weight(1f),
                model = model,
                spacing = spacing,
            )
        }
    }
}

@Composable
private fun RelatedPreviewRowDetails(
    modifier: Modifier = Modifier,
    model: RelatedPreviewModel,
    spacing: SmartFeedSpacing,
) {
    val formattedUpdatedAt = rememberFormattedUpdatedAt(model.updatedAt)

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
    ) {
        Text(
            text = model.title,
            modifier =
            Modifier.smartFeedSharedBounds(
                sharedTitleKey(model.id),
                model.id,
            ),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (model.summary.isNotBlank()) {
            Text(
                text = model.summary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = formattedUpdatedAt,
            modifier =
            Modifier.smartFeedSharedBounds(
                sharedUpdatedAtKey(model.id),
                model.id,
            ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (model.tags.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(spacing.xSmall),
                verticalArrangement = Arrangement.spacedBy(spacing.xSmall),
            ) {
                model.tags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        modifier =
                        Modifier.smartFeedSharedBounds(
                            sharedTagKey(model.id, tag),
                            model.id,
                        ),
                    )
                }
            }
        }
    }
}

private data class RelatedPreviewModel(
    val id: String,
    val imageUrl: String,
    val title: String,
    val summary: String,
    val updatedAt: com.core.content.model.UpdatedAt,
    val tags: List<String>,
)

private fun ContentItemPreview.toRelatedPreviewModel(): RelatedPreviewModel = when (this) {
    is ContentItemPreview.ArticlePreview ->
        RelatedPreviewModel(
            id = id.value,
            imageUrl = mainImageUrl.value,
            title = title.value,
            summary = short.value,
            updatedAt = updatedAt,
            tags = tags.value,
        )
    is ContentItemPreview.UnknownPreview ->
        RelatedPreviewModel(
            id = id.value,
            imageUrl = mainImageUrl.value,
            title = rawType,
            summary = "",
            updatedAt = updatedAt,
            tags = tags.value,
        )
}
