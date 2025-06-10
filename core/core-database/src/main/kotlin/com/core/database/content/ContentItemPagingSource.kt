package com.core.database.content

import androidx.paging.PagingSource
import androidx.room.RoomRawQuery
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query


fun contentItemPagingSource(
    query: Query,
    contentDao: ContentDao
): PagingSource<Int, ContentPreviewWithDetails> {
    val sqlBuilder = StringBuilder()
    val args = mutableListOf<Any>()

    // Начало запроса: выбираем из основной таблицы и присоединяем атрибуты статьи
    sqlBuilder.append(
        """
        SELECT DISTINCT content_updates.*
        FROM content_updates
        LEFT JOIN content_update_tags 
          ON  content_updates.id = content_update_tags.contentUpdateId
        LEFT JOIN article_attributes
          ON content_updates.id = article_attributes.contentUpdateId
        WHERE content_updates.action <> 'delete'
    """.trimIndent()
    )

    // Условия WHERE
    val whereClauses = mutableListOf<String>()

    // Фильтр по типам (если список непустой)
    if (query.types.isNotEmpty()) {
        // Для каждого типа генерируем "?"
        val placeholders = query.types.joinToString(separator = ",") { "?" }
        whereClauses += "content_updates.type IN ($placeholders)"
        args.addAll(query.types.map { it.toString() })
    }

    // Фильтр по тэгам (если список непустой)
    if (query.tags.value.isNotEmpty()) {
        val placeholders = query.tags.value.joinToString(separator = ",") { "?" }
        whereClauses += "content_update_tags.tagName IN ($placeholders)"
        args.addAll(query.tags.value.map { it })
    }

    // Собираем WHERE, если есть условия
    if (whereClauses.isNotEmpty()) {
//        sqlBuilder.append("\nWHERE ")
        sqlBuilder.append("\nAND ")
        sqlBuilder.append(whereClauses.joinToString(" AND "))
    }

    // ORDER BY в зависимости от варианта сортировки
    sqlBuilder.append("\nORDER BY ")
    when (query.sortedBy) {
        ContentItemsSortedType.ByNameAsc -> {
            // Сортируем по названию статьи по возрастанию
            sqlBuilder.append("article_attributes.title ASC")
        }

        ContentItemsSortedType.ByNameDesc -> {
            sqlBuilder.append("article_attributes.title DESC")
        }

        ContentItemsSortedType.ByDateNewestFirst -> {
            // Сортируем по дате обновления по убыванию (новые сверху)
            sqlBuilder.append("content_updates.updatedAt DESC")
        }

        ContentItemsSortedType.ByDateOldestFirst -> {
            sqlBuilder.append("content_updates.updatedAt ASC")
        }
    }

    // Создаем объект запроса Room
    val rawQuery = RoomRawQuery(
        sql = sqlBuilder.toString(),
        onBindStatement = { bind ->
            for ((index, arg) in args.withIndex()) {
                val bindIndex = index + 1
                when (arg) {
                    is String -> bind.bindText(bindIndex, arg)
                    is Long -> bind.bindLong(bindIndex, arg)
                    is Int -> bind.bindLong(bindIndex, arg.toLong())
                    is Double -> bind.bindDouble(bindIndex, arg)
                    is Float -> bind.bindDouble(bindIndex, arg.toDouble())
                    is ByteArray -> bind.bindBlob(bindIndex, arg)
                    else -> bind.bindText(bindIndex, arg.toString())
                }
            }
        }
    )


    // Возвращаем PagingSource, полученный из DAO
    return contentDao.getContent(rawQuery)
}
