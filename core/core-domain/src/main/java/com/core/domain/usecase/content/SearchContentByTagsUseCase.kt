package com.core.domain.usecase.content

import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import kotlinx.coroutines.flow.Flow

/**
 * Use case для поиска контента по тегам
 */
class SearchContentByTagsUseCase(
    private val getContentUseCase: GetContentUseCase
) {
    operator fun invoke(
        tags: Tags,
        sortedBy: ContentItemsSortedType = ContentItemsSortedType.ByDateNewestFirst
    ): Flow<PagingData<ContentItem>> {
        val searchQuery = Query(
            types = ContentItemType.entries,
            tags = tags,
            sortedBy = sortedBy
        )
        return getContentUseCase(searchQuery)
    }
}