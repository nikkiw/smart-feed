package com.core.domain.repository


import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemType
import com.core.domain.model.Tags
import kotlinx.coroutines.flow.Flow

enum class ContentItemsSortedType {
    ByNameAsc,
    ByNameDesc,
    ByDateNewestFirst,
    ByDateOldestFirst
}

data class Query(
    val types: List<ContentItemType>,
    val tags: Tags,
    val sortedBy: ContentItemsSortedType
)


/**
 * Репозиторий, отвечающий за получение и сохранение обновлений контента.
 */
interface ContentItemRepository {
    /**
     * Возвращает Flow локальных контент-обновлений (с деталями) с поддержкой Paging.
     */
    fun flowContent(query: Query): Flow<PagingData<ContentItem>>

    /**
     * Синхронизирует контент: делает запрос в сеть, а затем накатывает изменения в базу.
     * @return true, если синхронизация прошла успешно, false — если была ошибка.
     */
    suspend fun syncContent(): Result<Unit>
}
