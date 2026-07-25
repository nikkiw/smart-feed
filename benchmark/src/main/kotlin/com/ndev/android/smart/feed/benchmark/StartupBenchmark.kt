package com.ndev.android.smart.feed.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class StartupBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val renderer = ArticleCardRenderer.fromInstrumentationArguments()

    @Before
    fun prepareData() {
        prepareFeed(
            device = device,
            warmScrollCache = false,
            renderer = renderer,
        )
    }

    @Test
    fun coldStartupNoCompilation() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.None(),
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = {
            pressHome()
        },
    ) {
        startActivityAndWait { launchIntent ->
            launchIntent.putExtra(ARTICLE_CARD_RENDERER_EXTRA, renderer.wireValue)
        }
    }

    @OptIn(androidx.benchmark.macro.ExperimentalMacrobenchmarkApi::class)
    @Test
    fun coldStartupWithProfile() = benchmarkRule.measureRepeated(
        packageName = TARGET_PACKAGE,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Ignore(),
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = {
            pressHome()
        },
    ) {
        startActivityAndWait { launchIntent ->
            launchIntent.putExtra(ARTICLE_CARD_RENDERER_EXTRA, renderer.wireValue)
        }
    }
}
