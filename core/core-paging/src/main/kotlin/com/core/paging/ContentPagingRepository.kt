package com.core.paging

import androidx.paging.PagingData
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.Query
import kotlinx.coroutines.flow.Flow

interface ContentPagingRepository {
    fun flowContent(query: Query): Flow<PagingData<ContentItemPreview>>
}
