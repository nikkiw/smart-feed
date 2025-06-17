package com.core.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit Rule, который подменяет Dispatchers.Main на TestDispatcher
 * и предоставляет удобный runBlockingTest-подобный метод.
 */
class CoroutineTestRule : TestWatcher() {
    private val testDispatcher = StandardTestDispatcher()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }

    /**
     * Обёртка для kotlinx.coroutines.test.runTest
     */
    fun runBlockingTest(block: suspend TestScope.() -> Unit) =
        runTest { block() }
}
