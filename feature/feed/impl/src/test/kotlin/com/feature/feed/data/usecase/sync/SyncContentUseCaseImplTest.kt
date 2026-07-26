package com.feature.feed.data.usecase.sync

import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.recommendation.domain.service.Recommender
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncContentUseCaseImplTest {
    private val repository = mockk<ContentItemRepository>()
    private val recommender = mockk<Recommender>()

    @Test
    fun `successful sync rebuilds user and article recommendations`() = runTest {
        coEvery { repository.syncContent() } returns Result.success(Unit)
        coEvery { recommender.updateRecommendationsForUser() } just Runs
        coEvery { recommender.updateRecommendationsForArticles() } just Runs
        val useCase = createUseCase()

        assertThat(useCase()).isEqualTo(Result.success(Unit))
        coVerify(exactly = 1) { repository.syncContent() }
        coVerify(exactly = 1) { recommender.updateRecommendationsForUser() }
        coVerify(exactly = 1) { recommender.updateRecommendationsForArticles() }
    }

    @Test
    fun `failed content sync does not rebuild recommendations`() = runTest {
        val failure = IllegalStateException("network")
        coEvery { repository.syncContent() } returns Result.failure(failure)

        assertThat(createUseCase()().exceptionOrNull()).isSameInstanceAs(failure)
        coVerify(exactly = 0) { recommender.updateRecommendationsForUser() }
        coVerify(exactly = 0) { recommender.updateRecommendationsForArticles() }
    }

    @Test
    fun `recommendation failure is returned from sync`() = runTest {
        val failure = IllegalStateException("recommendations")
        coEvery { repository.syncContent() } returns Result.success(Unit)
        coEvery { recommender.updateRecommendationsForUser() } just Runs
        coEvery { recommender.updateRecommendationsForArticles() } throws failure

        assertThat(createUseCase()().exceptionOrNull()).isSameInstanceAs(failure)
    }

    @Test
    fun `configured dev failure is returned without starting repository sync`() = runTest {
        val failure = IllegalStateException("third refresh")
        val useCase = createUseCase(failurePolicy = ManualSyncFailurePolicy { failure })

        assertThat(useCase().exceptionOrNull()).isSameInstanceAs(failure)
        coVerify(exactly = 0) { repository.syncContent() }
        coVerify(exactly = 0) { recommender.updateRecommendationsForUser() }
        coVerify(exactly = 0) { recommender.updateRecommendationsForArticles() }
    }

    private fun createUseCase(failurePolicy: ManualSyncFailurePolicy = ManualSyncFailurePolicy { null }) =
        SyncContentUseCaseImpl(repository, failurePolicy, recommender)
}
