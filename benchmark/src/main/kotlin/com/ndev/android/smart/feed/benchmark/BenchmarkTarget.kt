package com.ndev.android.smart.feed.benchmark

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.ndev.android.smart.feed"
private const val FEED_SCREEN_TAG = "feed_screen"
private const val FEED_LIST_TAG = "feed_list"
private const val PREVIEW_CARD_TAG = "preview_card"
private const val ARTICLE_SCREEN_TAG = "article_screen"
private const val ARTICLE_CONTENT_TAG = "article_content"
private const val RECOMMENDATION_LIST_TAG = "recommendation_list"
private const val TAB_FEED_TAG = "tab_feed"
private const val TAB_RECOMMENDATIONS_TAG = "tab_recommendations"

private const val FEED_TIMEOUT_MS = 60_000L

internal fun UiDevice.waitForFeed(): UiObject2 =
    waitForTag(FEED_LIST_TAG, "Feed list")

internal fun UiDevice.waitForFeedScreen(): UiObject2 =
    waitForTag(FEED_SCREEN_TAG, "Feed screen")

internal fun UiDevice.waitForPreviewCard(): UiObject2 =
    waitForTag(PREVIEW_CARD_TAG, "Preview card")

internal fun UiDevice.waitForArticleScreen(): UiObject2 =
    waitForTag(ARTICLE_SCREEN_TAG, "Article screen")

internal fun UiDevice.waitForArticleContent(): UiObject2 =
    waitForTag(ARTICLE_CONTENT_TAG, "Article content")

internal fun UiDevice.waitForRecommendationList(): UiObject2 =
    waitForTag(RECOMMENDATION_LIST_TAG, "Recommendation list")

internal fun UiDevice.waitForRecommendationsTab(): UiObject2 =
    waitForTag(TAB_RECOMMENDATIONS_TAG, "Recommendations tab")

internal fun UiDevice.waitForFeedTab(): UiObject2 =
    waitForTag(TAB_FEED_TAG, "Feed tab")

private fun UiDevice.waitForTag(
    tag: String,
    elementName: String,
): UiObject2 =
    requireNotNull(wait(Until.findObject(By.res(tag)), FEED_TIMEOUT_MS)) {
        "$elementName was not found within $FEED_TIMEOUT_MS ms (tag=$tag)"
    }

/**
 * Launches the selected target variant once before measurements. The dev flavor
 * populates Room from its deterministic asset; the prod flavor performs its normal
 * production bootstrap. Optionally warms the list/image cache for the dev scroll test.
 */
internal fun prepareFeed(
    device: UiDevice,
    warmScrollCache: Boolean,
) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val launchIntent = checkNotNull(
        instrumentation.context.packageManager.getLaunchIntentForPackage(TARGET_PACKAGE),
    ) {
        "No launcher activity found for $TARGET_PACKAGE"
    }
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    device.executeShellCommand("am force-stop $TARGET_PACKAGE")
    instrumentation.context.startActivity(launchIntent)

    device.waitForFeedScreen()
    val feed = device.waitForFeed()
    device.waitForPreviewCard()
    device.waitForIdle()

    if (warmScrollCache) {
        feed.setGestureMargin(device.displayWidth / 5)
        repeat(3) {
            feed.fling(Direction.DOWN)
            device.waitForIdle()
        }
        repeat(3) {
            feed.fling(Direction.UP)
            device.waitForIdle()
        }
        device.waitForPreviewCard()
    }

    device.pressHome()
    device.executeShellCommand("am force-stop $TARGET_PACKAGE")
}
