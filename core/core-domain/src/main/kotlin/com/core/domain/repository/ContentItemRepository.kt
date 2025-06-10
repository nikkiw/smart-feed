package com.core.domain.repository


import androidx.paging.PagingData
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.model.ContentItemPreview
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
     * Возвращает Flow контент-обновлений (с деталями) с поддержкой Paging.
     */
    fun flowContent(query: Query): Flow<PagingData<ContentItemPreview>>

    /**
     * Возвращает Result<ContentItem> для указанного itemId
     */
    suspend fun getContentById(itemId: ContentItemId): Result<ContentItem>

    /*
     * Возвращает true - если база пуста, false - если в ней есть данные
     */
    suspend fun isEmpty(): Boolean

    /**
     * Возвращает Flow списка тегов
     */
    fun flowAllTags(): Flow<Tags>

    /**
     * Синхронизирует контент: делает запрос в сеть, а затем накатывает изменения в базу.
     * @return true, если синхронизация прошла успешно, false — если была ошибка.
     */
    suspend fun syncContent(): Result<Unit>
}
