package com.core.analytics.impl

import com.core.analytics.api.AnalyticsService
import com.core.analytics.local.EventLogDao
import com.core.analytics.local.entity.EventLog
import com.core.analytics.local.entity.EventType
import com.core.content.model.ContentId
import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.feature.userprofile.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AnalyticsServiceImpl
    @Inject
    constructor(
        private val eventLogDao: EventLogDao,
        private val userProfileRepository: UserProfileRepository,
        @ApplicationScope private val applicationScope: CoroutineScope,
        @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : AnalyticsService {
        override fun trackEventReadContent(
            contentId: ContentId,
            readingTimeMillis: Long,
            readPercentage: Float,
        ) {
            applicationScope.launch(defaultDispatcher) {
                val event =
                    EventLog(
                        contentId = contentId.value,
                        eventType = EventType.READ,
                        readingTimeMillis = readingTimeMillis,
                        readPercentage = readPercentage,
                    )
                withContext(ioDispatcher) {
                    eventLogDao.insertEvent(event)
                    userProfileRepository.onArticleVisited(contentId)
                }
            }
        }
    }
