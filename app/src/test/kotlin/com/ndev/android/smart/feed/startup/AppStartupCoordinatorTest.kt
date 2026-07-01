package com.ndev.android.smart.feed.startup

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class AppStartupCoordinatorTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `does not bootstrap before lifecycle reaches started`() =
        runTest(dispatcher) {
            val bootstrapper = RecordingBootstrapper()
            val reporter = RecordingStartupErrorReporter()
            val owner = TestLifecycleOwner()
            val coordinator = AppStartupCoordinator(bootstrapper, reporter)

            coordinator.attach(owner)
            advanceUntilIdle()

            assertEquals(0, bootstrapper.calls)
        }

    @Test
    fun `bootstraps once when lifecycle reaches started`() =
        runTest(dispatcher) {
            val bootstrapper = RecordingBootstrapper()
            val reporter = RecordingStartupErrorReporter()
            val owner = TestLifecycleOwner()
            val coordinator = AppStartupCoordinator(bootstrapper, reporter)

            coordinator.attach(owner)
            owner.moveToStarted()
            advanceUntilIdle()

            assertEquals(1, bootstrapper.calls)
        }

    @Test
    fun `attaching twice still starts only one bootstrap job`() =
        runTest(dispatcher) {
            val bootstrapper = RecordingBootstrapper()
            val reporter = RecordingStartupErrorReporter()
            val owner = TestLifecycleOwner()
            val coordinator = AppStartupCoordinator(bootstrapper, reporter)

            coordinator.attach(owner)
            coordinator.attach(owner)
            owner.moveToStarted()
            advanceUntilIdle()

            assertEquals(1, bootstrapper.calls)
        }

    @Test
    fun `reports bootstrap failure`() =
        runTest(dispatcher) {
            val failure = IllegalStateException("startup failed")
            val bootstrapper = RecordingBootstrapper { throw failure }
            val reporter = RecordingStartupErrorReporter()
            val owner = TestLifecycleOwner()
            val coordinator = AppStartupCoordinator(bootstrapper, reporter)

            coordinator.attach(owner)
            owner.moveToStarted()
            advanceUntilIdle()

            assertEquals(1, reporter.errors.size)
            assertEquals(failure, reporter.errors.single())
        }

    private class RecordingBootstrapper(
        private val onBootstrap: suspend () -> Unit = {},
    ) : AppBootstrapper {
        var calls: Int = 0
            private set

        override suspend fun bootstrap() {
            calls += 1
            onBootstrap()
        }
    }

    private class RecordingStartupErrorReporter : StartupErrorReporter {
        val errors = mutableListOf<Throwable>()

        override fun reportStartupFailure(error: Throwable) {
            errors += error
        }
    }

    private class TestLifecycleOwner : LifecycleOwner {
        private val registry = LifecycleRegistry(this)

        override val lifecycle: Lifecycle = registry

        fun moveToStarted() {
            registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        }
    }
}
