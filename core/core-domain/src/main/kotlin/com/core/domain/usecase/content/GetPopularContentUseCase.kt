package com.core.domain.usecase.content

import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemPreview
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

/**
 * Use case для получения контента с предустановленными популярными фильтрами
 */
@Singleton
class GetPopularContentUseCase(
    private val getContentUseCase: GetContentUseCase
) {
    operator fun invoke(): Flow<PagingData<ContentItemPreview>> {
        val popularQuery = Query(
            types = ContentItemType.entries,
            tags = Tags(),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )
        return getContentUseCase(popularQuery)
    }
}