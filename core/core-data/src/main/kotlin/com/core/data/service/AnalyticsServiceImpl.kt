package com.core.data.service

import com.core.database.event.entity.EventLog
import com.core.database.event.EventLogDao
import com.core.database.event.entity.EventType
import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.core.domain.model.ContentId
import com.core.domain.repository.UserProfileRepository
import com.core.domain.service.AnalyticsService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnalyticsServiceImpl @Inject constructor(
    private val eventLogDao: EventLogDao,
    private val userProfileRepository: UserProfileRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AnalyticsService {
    override fun trackEventReadContent(
        contentId: ContentId,
        readingTimeMillis: Long,
        readPercentage: Float
    ) {
        applicationScope.launch(defaultDispatcher) {
            val event = EventLog(
                contentId = contentId.value,
                eventType = EventType.READ,
                readingTimeMillis = readingTimeMillis,
                readPercentage = readPercentage
            )
            withContext(ioDispatcher) {
                eventLogDao.insertEvent(event)
                userProfileRepository.onArticleVisited(contentId)
            }
        }
    }
}
