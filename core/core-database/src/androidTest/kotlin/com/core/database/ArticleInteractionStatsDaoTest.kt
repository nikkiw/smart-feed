package com.core.database

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.event.entity.EventLog
import com.core.database.event.EventLogDao
import com.core.database.event.entity.EventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class ArticleInteractionStatsDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var statsDao: ContentInteractionStatsDao
    private lateinit var eventDao: EventLogDao

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getTestDatabase(context)
        statsDao = db.articleInteractionStatsDao()
        eventDao = db.eventLogDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testArticleStatsDao_empty() = runTest {
        // Initially no stats
        val stats = statsDao.getStatsForContent("1")
        assertNull(stats)
    }

    @Test
    fun testInsertReadEvent_updatesStatsTable() = runTest {
        val articleId = "42"
        // Insert READ event
        val readTime = 500L
        val readPerc = 0.8f
        val event = EventLog(
            contentId = articleId,
            eventType = EventType.READ,
            readingTimeMillis = readTime,
            readPercentage = readPerc
        )
        eventDao.insertEvent(event)

        // Stats should be created
        val stats = statsDao.getStatsForContent(articleId)
        assertNotNull(stats)
        stats.let {
            assertEquals(articleId, it.contentId)
            assertEquals(1, it.readCount)
            assertEquals(readTime.toDouble(), it.avgReadingTime, 0.001)
            assertEquals(readPerc.toDouble(), it.avgReadPercentage, 0.001)
        }

        // Insert another READ event for same article
        val secondTime = 1500L
        val secondPerc = 0.5f
        val event2 = EventLog(
            contentId = articleId,
            eventType = EventType.READ,
            readingTimeMillis = secondTime,
            readPercentage = secondPerc
        )
        eventDao.insertEvent(event2)

        // Stats should update averages
        val updated = statsDao.getStatsForContent(articleId)
        assertNotNull(updated)

        assertEquals(2, updated.readCount)
        val expectedAvgTime = (readTime + secondTime) / 2.0
        val expectedAvgPerc = (readPerc + secondPerc) / 2.0
        assertEquals(expectedAvgTime, updated.avgReadingTime, 0.001)
        assertEquals(expectedAvgPerc, updated.avgReadPercentage, 0.001)
    }

    @Test
    fun testGetTopArticlesByReadCount() = runTest {
        // Prepopulate stats
        val articleId1 = "1"
        // Insert READ event
        val readTime = 500L
        val readPerc = 0.8f
        val event = EventLog(
            contentId = articleId1,
            eventType = EventType.READ,
            readingTimeMillis = readTime,
            readPercentage = readPerc
        )
        eventDao.insertEvent(event)
        eventDao.insertEvent(event)
        eventDao.insertEvent(event)

        val articleId2 = "2"
        eventDao.insertEvent(
            EventLog(
                contentId = articleId2,
                eventType = EventType.READ,
                readingTimeMillis = readTime,
                readPercentage = readPerc
            )
        )

        val articleId3 = "3"
        eventDao.insertEvent(
            EventLog(
                contentId = articleId3,
                eventType = EventType.READ,
                readingTimeMillis = readTime,
                readPercentage = readPerc
            )
        )
        eventDao.insertEvent(
            EventLog(
                contentId = articleId3,
                eventType = EventType.READ,
                readingTimeMillis = readTime,
                readPercentage = readPerc
            )
        )
        eventDao.insertEvent(
            EventLog(
                contentId = articleId3,
                eventType = EventType.READ,
                readingTimeMillis = readTime,
                readPercentage = readPerc
            )
        )
        eventDao.insertEvent(
            EventLog(
                contentId = articleId3,
                eventType = EventType.READ,
                readingTimeMillis = readTime,
                readPercentage = readPerc
            )
        )


        val top2 = statsDao.getTopContentByReadCount(2)
        assertEquals(listOf(articleId3, articleId1), top2.map { it.contentId })
    }

    @Test
    fun testEventLogDao_countAndFlow() = runTest {
        val articleId = "100"
        // Insert various events
        val events = listOf(
            EventLog(contentId = articleId, eventType = EventType.IMPRESSION),
            EventLog(contentId = articleId, eventType = EventType.READ),
            EventLog(contentId = articleId, eventType = EventType.CLICK)
        )
        eventDao.insertEvents(events)

        // Test count
        val countImpressions = eventDao.countEventsForContent(articleId, EventType.IMPRESSION)
        assertEquals(1, countImpressions)

        val countReads = eventDao.countEventsForContent(articleId, EventType.READ)
        assertEquals(1, countReads)

        // Test getEventsForArticle flow
        val firstList = eventDao.getEventsForContent(articleId).first()
        assertEquals(3, firstList.size)

        // Test getRecentEvents flow
        val recent = eventDao.getRecentEvents(2).first()
        assertEquals(2, recent.size)

    }
}
