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

    @Test
    fun generate() = baselineRule.collect(
        packageName = TARGET_PACKAGE,
        profileBlock = {
            val gestureMargin = device.displayWidth / 5

            startActivityAndWait()
            val feed = device.waitForFeed().apply {
                setGestureMargin(gestureMargin)
            }
            device.waitForPreviewCard()
            device.waitForIdle()

            repeat(2) {
                feed.fling(androidx.test.uiautomator.Direction.DOWN)
                device.waitForIdle()
            }

            requireNotNull(feed.findObject(androidx.test.uiautomator.By.res("preview_card"))) {
                "No preview card was found inside the feed list"
            }.click()

            val article = device.waitForArticleContent().apply {
                setGestureMargin(gestureMargin)
            }
            device.waitForArticleScreen()
            device.waitForIdle()

            repeat(2) {
                article.fling(androidx.test.uiautomator.Direction.DOWN)
                device.waitForIdle()
            }

            device.pressBack()
            device.waitForFeedScreen()
            device.waitForFeed()
            device.waitForPreviewCard()
            device.waitForIdle()

            device.waitForRecommendationsTab().click()
            val recommendationList = device.waitForRecommendationList().apply {
                setGestureMargin(gestureMargin)
            }
            device.waitForPreviewCard()
            device.waitForIdle()
            recommendationList.fling(androidx.test.uiautomator.Direction.DOWN)
            device.waitForIdle()

            device.waitForFeedTab().click()
            val feedAfterTabSwitch = device.waitForFeed().apply {
                setGestureMargin(gestureMargin)
            }
            feedAfterTabSwitch.fling(androidx.test.uiautomator.Direction.UP)
            device.waitForIdle()
        }
    )
}
