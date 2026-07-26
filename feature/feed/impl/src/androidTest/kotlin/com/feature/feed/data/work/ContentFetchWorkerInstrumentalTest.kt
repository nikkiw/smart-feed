package com.feature.feed.data.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.core.image.ImageLoader
import com.feature.feed.data.di.FeedDataModule
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import javax.inject.Singleton

@UninstallModules(FeedDataModule::class)
@HiltAndroidTest
class ContentFetchWorkerInstrumentalTest {
    @Module
    @InstallIn(SingletonComponent::class)
    abstract class WorkerTestModule {
        @Binds
        @Singleton
        abstract fun bindSyncContentUseCase(useCase: FakeSyncContentUseCase): SyncContentUseCase

        @Binds
        @Singleton
        abstract fun bindImageLoader(loader: FakeImageLoader): ImageLoader
    }

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakeSyncContentUseCase: FakeSyncContentUseCase

    private lateinit var context: Context

    @Before
    fun setUp() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }

    private fun buildWorker(): ContentFetchWorker {
        return TestListenableWorkerBuilder<ContentFetchWorker>(context)
            .setWorkerFactory(HiltWorkerFactoryForTest.create(context))
            .build()
    }

    @Test
    fun doWork_successful_invokes_orchestrated_sync_and_returns_success() = runTest {
        // arrange
        fakeSyncContentUseCase.shouldFail = false

        // act
        val worker = buildWorker()
        val result = worker.doWork()

        // assert
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakeSyncContentUseCase.invoked.get()).isTrue()
    }

    @Test
    fun doWork_failure_returns_failure_with_error_message() = runTest {
        // arrange
        fakeSyncContentUseCase.shouldFail = true

        // act
        val worker = buildWorker()
        val result = worker.doWork()

        // assert
        assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
        val failure = result as ListenableWorker.Result.Failure
        val msg = failure.outputData.getString(ContentFetchWorker.KEY_ERROR_MESSAGE)
        assertThat(msg).isEqualTo("Test failure")
    }
}
