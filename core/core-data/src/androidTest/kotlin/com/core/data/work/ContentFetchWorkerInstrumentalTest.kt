package com.core.data.work

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.core.data.di.DataModule
import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.Recommender
import com.core.image.ImageLoader
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

@UninstallModules(DataModule::class)
@HiltAndroidTest
class ContentFetchWorkerInstrumentalTest {

    @Module
    @InstallIn(SingletonComponent::class)
    abstract class WorkerTestModule {
        @Binds
        @Singleton
        abstract fun bindContentRepo(repo: FakeContentRepo): ContentItemRepository

        @Binds
        @Singleton
        abstract fun bindRecommender(rec: FakeRecommender): Recommender

    }


    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var fakeRepo: FakeContentRepo
    @Inject
    lateinit var fakeRec: FakeRecommender

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
    fun doWork_successful_invokes_sync_and_recommender_and_returns_success() = runTest {
        // arrange
        fakeRepo.shouldFail = false

        // act
        val worker = buildWorker()
        val result = worker.doWork()

        // assert
        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        assertThat(fakeRec.updatedUser.get()).isTrue()
        assertThat(fakeRec.updatedArticles.get()).isTrue()
    }

    @Test
    fun doWork_failure_returns_failure_with_error_message() = runTest {
        // arrange
        fakeRepo.shouldFail = true

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
