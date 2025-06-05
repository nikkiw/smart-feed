package com.core.domain.usecase.content

import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.Query
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case для получения контента с пагинацией и фильтрацией
 */
@ViewModelScoped
class GetContentUseCase @Inject constructor(
    private val contentItemRepository: ContentItemRepository
) {
    operator fun invoke(query: Query): Flow<PagingData<ContentItem>> {
        return contentItemRepository.flowContent(query)
    }
}