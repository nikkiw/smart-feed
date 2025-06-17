package com.core.data.service

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.core.data.di.CoroutinesTestModule
import com.core.database.AppDatabase
import com.core.database.event.ContentInteractionStatsDao
import com.core.domain.model.ContentId
import com.core.domain.service.AnalyticsService
import com.google.common.truth.Truth.assertThat
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject
import kotlin.test.assertNotNull
import kotlin.test.assertNull


@HiltAndroidTest
class AnalyticsServiceImplTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var context: Context

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = CoroutinesTestModule.testDispatcher

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var db: AppDatabase

    @Inject
    lateinit var contentInteractionStatsDao: ContentInteractionStatsDao

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        hiltRule.inject()
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.clearAllTables()
    }

    @Test
    fun returnsNull_whenNoReadEventsTracked() = runTest(testDispatcher) {
        // act
        val id = "id1"
        val result = contentInteractionStatsDao.getStatsForContent(id)

        // assert
        assertNull(result)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun returnsCorrectStats_afterSingleReadEventTracked() = runTest {
        // act
        val id = "id1"
        analyticsService.trackEventReadContent(
            contentId = ContentId(id),
            readingTimeMillis = 30000L,
            readPercentage = 0.9f
        )
        advanceUntilIdle()


        val result = contentInteractionStatsDao.getStatsForContent(id)
        // assert
        assertNotNull(result)

        assertThat(result.contentId).isEqualTo(id)
        assertThat(result.readCount).isEqualTo(1)
        assertThat(result.avgReadingTime).isWithin(1e-6).of(30000.0)
        assertThat(result.avgReadPercentage).isWithin(1e-6).of(0.9)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun returnsCorrectAggregatedStats_afterMultipleReadEvents() = runTest(testDispatcher) {
        val id = "id1"
        analyticsService.trackEventReadContent(
            contentId = ContentId(id),
            readingTimeMillis = 30000L,
            readPercentage = 0.9f
        )
        advanceUntilIdle()

        // Дополнительная проверка после первого события
        // Проверим, что событие сохранилось в EventLog
        val events = db.eventLogDao().getEventsForContent(id).first() // или другой подходящий метод
        assertThat(events).hasSize(1)

        val intermediateResult = contentInteractionStatsDao.getStatsForContent(id)
        assertNotNull(intermediateResult)
        assertThat(intermediateResult.readCount).isEqualTo(1)


        analyticsService.trackEventReadContent(
            contentId = ContentId(id),
            readingTimeMillis = 10000L,
            readPercentage = 0.7f
        )
        advanceUntilIdle()

        val eventsTwo =
            db.eventLogDao().getEventsForContent(id).first() // или другой подходящий метод
        assertThat(eventsTwo).hasSize(2)


        val result = contentInteractionStatsDao.getStatsForContent(id)

        assertNotNull(result)
        assertThat(result.contentId).isEqualTo(id)
        assertThat(result.readCount).isEqualTo(2)
        assertThat(result.avgReadingTime).isWithin(1e-6).of(20000.0)
        assertThat(result.avgReadPercentage).isWithin(1e-6).of(0.8)
    }


}