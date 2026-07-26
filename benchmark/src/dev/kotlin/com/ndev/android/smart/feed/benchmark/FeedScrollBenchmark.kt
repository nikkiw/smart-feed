package com.ndev.android.smart.feed.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.ExperimentalMetricApi
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.MemoryUsageMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class FeedScrollBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun prepareDataAndCaches() {
        prepareFeed(
            device = device,
            warmScrollCache = true,
        )
    }

    @Test
    fun feedListScrollNoCompilation() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            // setupBlock runs BEFORE the framework kills the process for a cold start.
            // Only navigate away here; startActivityAndWait() belongs in the measure
            // block so it is called after the cold relaunch.
            setupBlock = {
                pressHome()
            },
        ) {
            // With COLD startup the framework kills the process after setupBlock and
            // wait for the feed to be ready on every iteration.
            scrollFeed()
        }
    }

    @OptIn(androidx.benchmark.macro.ExperimentalMacrobenchmarkApi::class)
    @Test
    fun feedListScrollWithProfile() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Ignore(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = {
                pressHome()
            },
        ) {
            scrollFeed()
        }
    }


    @OptIn(ExperimentalMetricApi::class)
    @Test
    fun feedListMemoryNoCompilation() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(MemoryUsageMetric(MemoryUsageMetric.Mode.Max)),
            compilationMode = CompilationMode.None(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = { pressHome() },
        ) {
            scrollFeed()
        }
    }

    @OptIn(ExperimentalMetricApi::class, androidx.benchmark.macro.ExperimentalMacrobenchmarkApi::class)
    @Test
    fun feedListMemoryWithProfile() {
        benchmarkRule.measureRepeated(
            packageName = TARGET_PACKAGE,
            metrics = listOf(MemoryUsageMetric(MemoryUsageMetric.Mode.Max)),
            compilationMode = CompilationMode.Ignore(),
            startupMode = StartupMode.COLD,
            iterations = 10,
            setupBlock = { pressHome() },
        ) {
            scrollFeed()
        }
    }

    private fun MacrobenchmarkScope.scrollFeed() {
        startActivityAndWait()
        device.waitForFeedScreen()
        val feed = device.waitForFeed().apply {
            setGestureMargin(device.displayWidth / 5)
        }
        device.waitForPreviewCard()
        device.waitForIdle()

        repeat(5) {
            feed.fling(Direction.DOWN)
            device.waitForIdle()
        }
        repeat(5) {
            feed.fling(Direction.UP)
            device.waitForIdle()
        }
    }
}
