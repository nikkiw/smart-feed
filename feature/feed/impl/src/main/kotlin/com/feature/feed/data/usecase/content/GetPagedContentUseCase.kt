package com.feature.feed.data.usecase.content

import androidx.paging.PagingData
import com.feature.feed.data.repository.ContentPagingRepository
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedContentUseCase
    @Inject
    constructor(
        private val contentPagingRepository: ContentPagingRepository,
    ) {
        operator fun invoke(query: Query): Flow<PagingData<ContentItemPreview>> =
            contentPagingRepository.flowContent(query)
    }
