package com.feature.feed.compose

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Shape
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
internal val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

internal val LocalAnimatedVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

internal val LocalSharedTransitionContentId = staticCompositionLocalOf<MutableState<String?>?> { null }

internal val SmartFeedBoundsTransform =
    BoundsTransform { _, _ ->
        tween(durationMillis = 700, easing = FastOutSlowInEasing)
    }

@OptIn(ExperimentalSharedTransitionApi::class)
internal fun Modifier.smartFeedSharedBounds(
    contentKey: String,
    contentId: String,
    clipShape: Shape? = null,
    boundsTransform: BoundsTransform = SmartFeedBoundsTransform,
): Modifier = composed {
    val baseModifier = this
    val transitionContentId = LocalSharedTransitionContentId.current?.value
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current

    if (
        transitionContentId != contentId ||
        sharedTransitionScope == null ||
        animatedVisibilityScope == null
    ) {
        this
    } else {
        with(sharedTransitionScope) {
            val sharedContentState = rememberSharedContentState(sharedContentKey(contentKey))

            if (clipShape == null) {
                baseModifier.sharedBounds(
                    sharedContentState = sharedContentState,
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = boundsTransform,
                )
            } else {
                baseModifier.sharedBounds(
                    sharedContentState = sharedContentState,
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = boundsTransform,
                    clipInOverlayDuringTransition = OverlayClip(clipShape),
                )
            }
        }
    }
}

@Composable
internal fun rememberSharedTransitionNavigation(contentId: String, onNavigate: () -> Unit): () -> Unit {
    val sharedTransitionContentId = LocalSharedTransitionContentId.current
    val latestOnNavigate by rememberUpdatedState(onNavigate)
    val coroutineScope = rememberCoroutineScope()

    return {
        sharedTransitionContentId?.value = contentId
        coroutineScope.launch {
            // Let the source card recompose with shared bounds before navigation swaps the destination.
            withFrameNanos { }
            latestOnNavigate()
        }
    }
}

internal fun sharedContentKey(contentId: String): String = "transition_content_$contentId"

internal fun sharedCardKey(contentId: String): String = "card_$contentId"

internal fun sharedImageKey(contentId: String): String = "image_$contentId"

internal fun sharedTitleKey(contentId: String): String = "title_$contentId"

internal fun sharedUpdatedAtKey(contentId: String): String = "updated_at_$contentId"

internal fun sharedTagKey(contentId: String, tag: String): String = "tag_${contentId}_$tag"
