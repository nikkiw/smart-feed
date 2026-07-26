package com.feature.feed.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.feature.feed.root.FeedRootComponent
import com.feature.feed.root.FeedRootComponent.Config

private data class FeedRootDestination(
    val config: Config,
    val child: FeedRootComponent.Child,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FeedRootContent(component: FeedRootComponent, modifier: Modifier = Modifier) {
    SmartFeedTheme {
        Surface(
            modifier =
            modifier
                .fillMaxSize()
                .semantics { testTagsAsResourceId = true }
                .testTag(SmartFeedUiTags.ROOT),
            color = MaterialTheme.colorScheme.background,
        ) {
            val childStack by component.childStack.subscribeAsState()
            val destination =
                remember(childStack.active.configuration, childStack.active.instance) {
                    FeedRootDestination(
                        config = childStack.active.configuration as Config,
                        child = childStack.active.instance,
                    )
                }
            var bottomBarVisible by remember { mutableStateOf(true) }
            val sharedTransitionContentId = remember { mutableStateOf<String?>(null) }

            LaunchedEffect(destination.child) {
                // Show bottom bar only on top-level screens; hide it immediately for detail screens
                bottomBarVisible = destination.child is FeedRootComponent.Child.FeedScreen ||
                    destination.child is FeedRootComponent.Child.RecommendationScreen
            }

            SharedTransitionLayout {
                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = destination,
                        transitionSpec = {
                            rootContentTransform(
                                initial = initialState.config,
                                target = targetState.config,
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        label = "feed-root-content",
                    ) animatedContent@{ target ->
                        CompositionLocalProvider(
                            LocalSharedTransitionScope provides this@SharedTransitionLayout,
                            LocalAnimatedVisibilityScope provides this@animatedContent,
                            LocalSharedTransitionContentId provides sharedTransitionContentId,
                        ) {
                            when (val child = target.child) {
                                is FeedRootComponent.Child.FeedScreen ->
                                    FeedMasterScreen(
                                        component = child.component,
                                        onChromeVisibilityChanged = { bottomBarVisible = it },
                                    )
                                is FeedRootComponent.Child.ArticleScreen ->
                                    ArticleScreen(
                                        component = child.component,
                                        routePreview = child.preview,
                                    )
                                is FeedRootComponent.Child.RecommendationScreen ->
                                    RecommendationScreen(
                                        component = child.component,
                                        onChromeVisibilityChanged = { bottomBarVisible = it },
                                    )
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = bottomBarVisible,
                        modifier = Modifier.align(Alignment.BottomCenter),
                        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut(),
                    ) {
                        FeedBottomBar(
                            component = component.bottomBar,
                            // No navigationBarsPadding() here — Material3 NavigationBar handles
                            // its own windowInsets and draws its surface color behind system buttons
                        )
                    }
                }
            }
        }
    }
}

private fun rootContentTransform(initial: Config, target: Config): ContentTransform = when {
    initial is Config.FeedScreenConfig && target is Config.RecommendationScreenConfig ->
        (slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn())
            .togetherWith(slideOutHorizontally(targetOffsetX = { -it / 6 }) + fadeOut())
    initial is Config.RecommendationScreenConfig && target is Config.FeedScreenConfig ->
        (slideInHorizontally(initialOffsetX = { -it / 6 }) + fadeIn())
            .togetherWith(slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut())
    else ->
        fadeIn(animationSpec = tween(durationMillis = 700))
            .togetherWith(fadeOut(animationSpec = tween(durationMillis = 550)))
}
