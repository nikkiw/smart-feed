package com.feature.feed.data.usecase.sync

import com.feature.feed.domain.repository.ContentItemRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SyncContentUseCaseImplTest {
    private val repository = mockk<ContentItemRepository>()

    @Test
    fun `manual sync calls repository directly`() =
        runTest {
            coEvery { repository.syncContent() } returns Result.success(Unit)
            val useCase = SyncContentUseCaseImpl(repository, ManualSyncFailurePolicy { null })

            assertThat(useCase()).isEqualTo(Result.success(Unit))
            coVerify(exactly = 1) { repository.syncContent() }
        }

    @Test
    fun `configured dev failure is returned without starting repository sync`() =
        runTest {
            val failure = IllegalStateException("third refresh")
            val useCase = SyncContentUseCaseImpl(repository, ManualSyncFailurePolicy { failure })

            assertThat(useCase().exceptionOrNull()).isSameInstanceAs(failure)
            coVerify(exactly = 0) { repository.syncContent() }
        }
}
