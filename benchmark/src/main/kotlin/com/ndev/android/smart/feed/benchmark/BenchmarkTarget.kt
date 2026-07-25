package com.ndev.android.smart.feed.benchmark

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.ndev.android.smart.feed"
internal const val ARTICLE_CARD_RENDERER_ARGUMENT = "articleCardRenderer"
internal const val ARTICLE_CARD_RENDERER_EXTRA =
    "com.ndev.android.smart.feed.extra.ARTICLE_CARD_RENDERER"

private const val FEED_RESOURCE_ID = "recyclerFeed"
private const val FEED_TIMEOUT_MS = 60_000L
private const val CONTENT_SETTLE_MS = 2_000L

internal enum class ArticleCardRenderer(
    val wireValue: String,
) {
    Xml("xml"),
    Compose("compose"),
    ;

    companion object {
        fun fromInstrumentationArguments(): ArticleCardRenderer {
            val value =
                InstrumentationRegistry.getArguments().getString(ARTICLE_CARD_RENDERER_ARGUMENT)
                    ?: Xml.wireValue
            return entries.firstOrNull { it.wireValue == value }
                ?: error(
                    "Unknown $ARTICLE_CARD_RENDERER_ARGUMENT=$value. " +
                        "Expected ${entries.joinToString { it.wireValue }}.",
                )
        }
    }
}

internal fun UiDevice.waitForFeed(): UiObject2 =
    requireNotNull(
        wait(
            Until.findObject(By.res(TARGET_PACKAGE, FEED_RESOURCE_ID)),
            FEED_TIMEOUT_MS,
        ),
    ) {
        "Feed RecyclerView was not found within $FEED_TIMEOUT_MS ms"
    }

/**
 * Launches the selected target variant once before measurements. The dev flavor
 * populates Room from its deterministic asset; the prod flavor performs its normal
 * production bootstrap. Optionally warms the list/image cache for the dev scroll test.
 */
internal fun prepareFeed(
    device: UiDevice,
    warmScrollCache: Boolean,
    renderer: ArticleCardRenderer,
) {
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val launchIntent = checkNotNull(
        instrumentation.context.packageManager.getLaunchIntentForPackage(TARGET_PACKAGE),
    ) {
        "No launcher activity found for $TARGET_PACKAGE"
    }
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        .putExtra(ARTICLE_CARD_RENDERER_EXTRA, renderer.wireValue)

    device.executeShellCommand("am force-stop $TARGET_PACKAGE")
    instrumentation.context.startActivity(launchIntent)

    val feed = device.waitForFeed()
    device.waitForIdle()
    Thread.sleep(CONTENT_SETTLE_MS)

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
        Thread.sleep(CONTENT_SETTLE_MS)
    }

    device.pressHome()
    device.executeShellCommand("am force-stop $TARGET_PACKAGE")
}
