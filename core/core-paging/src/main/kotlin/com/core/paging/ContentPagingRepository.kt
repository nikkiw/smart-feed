package com.core.paging

import androidx.paging.PagingData
import com.core.domain.model.ContentItemPreview
import com.core.domain.repository.Query
import kotlinx.coroutines.flow.Flow

interface ContentPagingRepository {
    fun flowContent(query: Query): Flow<PagingData<ContentItemPreview>>
}
