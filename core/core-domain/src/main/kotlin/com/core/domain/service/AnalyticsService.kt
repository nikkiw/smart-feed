package com.core.domain.service

import com.core.domain.model.ContentId

/**
 * Service interface for tracking user interaction events related to content.
 *
 * This interface is used to send analytics data (e.g., to a backend or analytics tool)
 * to measure how users engage with content items.
 */
interface AnalyticsService {

    /**
     * Tracks that a user has read a specific content item.
     *
     * @param contentId The unique identifier of the content that was read.
     * @param readingTimeMillis Total time (in milliseconds) the user spent reading the content.
     * @param readPercentage Approximate percentage of the content that was read, ranging from 0.0 to 1.0,
     *   where 0.0 means none and 1.0 means the full content was read.
     */
    fun trackEventReadContent(
        contentId: ContentId,
        readingTimeMillis: Long,
        readPercentage: Float
    )
}