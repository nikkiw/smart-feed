package com.ndev.android.smart.feed.benchmark

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {

    @get:Rule
    val baselineRule = BaselineProfileRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val renderer = ArticleCardRenderer.fromInstrumentationArguments()

    @Test
    fun generate() = baselineRule.collect(
        packageName = TARGET_PACKAGE,
        profileBlock = {
            startActivityAndWait { launchIntent ->
                launchIntent.putExtra(ARTICLE_CARD_RENDERER_EXTRA, renderer.wireValue)
            }
            val feed = device.waitForFeed().apply {
                setGestureMargin(device.displayWidth / 5)
            }
            device.waitForIdle()

            repeat(3) {
                feed.fling(androidx.test.uiautomator.Direction.DOWN)
                device.waitForIdle()
            }
        }
    )
}
