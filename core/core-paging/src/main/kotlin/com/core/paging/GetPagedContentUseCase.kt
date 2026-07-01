package com.core.paging

import androidx.paging.PagingData
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.Query
import kotlinx.coroutines.flow.Flow

interface GetPagedContentUseCase {
    operator fun invoke(query: Query): Flow<PagingData<ContentItemPreview>>
}
