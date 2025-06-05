package com.core.domain.usecase.content

import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import kotlinx.coroutines.flow.Flow

/**
 * Use case для получения контента с предустановленными популярными фильтрами
 */
class GetPopularContentUseCase(
    private val getContentUseCase: GetContentUseCase
) {
    operator fun invoke(): Flow<PagingData<ContentItem>> {
        val popularQuery = Query(
            types = ContentItemType.entries,
            tags = Tags(),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )
        return getContentUseCase(popularQuery)
    }
}