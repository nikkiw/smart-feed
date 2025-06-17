package com.core.data.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.core.data.CoroutineTestRule
import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.Recommender
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ContentFetchWorkerTest {

    @get:Rule
    val coroutineRule = CoroutineTestRule()

    private lateinit var context: Context
    private lateinit var repository: ContentItemRepository
    private lateinit var recommender: Recommender

    @Before
    fun setUp() {
        // Просто мокируем Context, он не используется внутри doWork()
        context = mockk(relaxed = true)
        repository = mockk()
        recommender = mockk()
    }

    @Test
    fun `doWork returns Success when syncContent and recommender succeed`() = coroutineRule.runBlockingTest {
        coEvery { repository.syncContent() } returns Result.success(Unit)
        coEvery { recommender.updateRecommendationsForUser() } just Runs
        coEvery { recommender.updateRecommendationsForArticles() } just Runs

        val worker = TestListenableWorkerBuilder<ContentFetchWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository, recommender))
            .build()

        val result = worker.startWork().get()
        assertTrue(result is ListenableWorker.Result.Success)

        coVerify { repository.syncContent() }
        coVerify { recommender.updateRecommendationsForUser() }
        coVerify { recommender.updateRecommendationsForArticles() }
    }

    @Test
    fun `doWork returns Failure and includes error message when syncContent fails`() = coroutineRule.runBlockingTest {
        val error = RuntimeException("Sync failed")
        coEvery { repository.syncContent() } returns Result.failure(error)

        val worker = TestListenableWorkerBuilder<ContentFetchWorker>(context)
            .setWorkerFactory(TestWorkerFactory(repository, recommender))
            .build()

        val result = worker.startWork().get()
        assertTrue(result is ListenableWorker.Result.Failure)

        val failure = result as ListenableWorker.Result.Failure
        val errorMsg = failure.outputData.getString(ContentFetchWorker.KEY_ERROR_MESSAGE)
        assertEquals("Sync failed", errorMsg)

        coVerify(exactly = 0) { recommender.updateRecommendationsForUser() }
        coVerify(exactly = 0) { recommender.updateRecommendationsForArticles() }
    }
}
