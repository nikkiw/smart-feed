package com.core.paging

import androidx.paging.PagingData
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedContentUseCaseImpl
    @Inject
    constructor(
        private val contentPagingRepository: ContentPagingRepository,
    ) : GetPagedContentUseCase {
        override operator fun invoke(query: Query): Flow<PagingData<ContentItemPreview>> =
            contentPagingRepository.flowContent(query)
    }
