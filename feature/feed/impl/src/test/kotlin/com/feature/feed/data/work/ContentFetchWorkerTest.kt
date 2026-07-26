package com.feature.feed.data.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.feature.feed.data.CoroutineTestRule
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContentFetchWorkerTest {
    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var context: Context
    private lateinit var syncContentUseCase: SyncContentUseCase

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        syncContentUseCase = mockk()
    }

    @Test
    fun `doWork returns Success when orchestrated sync succeeds`() = coroutineRule.runBlockingTest {
        coEvery { syncContentUseCase() } returns Result.success(Unit)

        val worker =
            TestListenableWorkerBuilder<ContentFetchWorker>(context)
                .setWorkerFactory(TestWorkerFactory(syncContentUseCase))
                .build()

        val result = worker.startWork().get()
        assertTrue(result is ListenableWorker.Result.Success)

        coVerify(exactly = 1) { syncContentUseCase() }
    }

    @Test
    fun `doWork returns Failure and includes error message when syncContent fails`() = coroutineRule.runBlockingTest {
        val error = RuntimeException("Sync failed")
        coEvery { syncContentUseCase() } returns Result.failure(error)

        val worker =
            TestListenableWorkerBuilder<ContentFetchWorker>(context)
                .setWorkerFactory(TestWorkerFactory(syncContentUseCase))
                .build()

        val result = worker.startWork().get()
        assertTrue(result is ListenableWorker.Result.Failure)

        val failure = result as ListenableWorker.Result.Failure
        val errorMsg = failure.outputData.getString(ContentFetchWorker.KEY_ERROR_MESSAGE)
        assertEquals("Sync failed", errorMsg)

        coVerify(exactly = 1) { syncContentUseCase() }
    }
}
