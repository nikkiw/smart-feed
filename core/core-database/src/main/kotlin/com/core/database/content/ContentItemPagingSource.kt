package com.core.database.content

import androidx.paging.PagingSource
import androidx.room.RoomRawQuery
import com.core.database.content.entity.ContentPreviewWithDetails
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query


/**
 * Builds a dynamic SQL query and returns a [PagingSource] for paginated content previews.
 *
 * This function dynamically constructs a SQL query based on the given [query] parameters,
 * including filtering by content types and tags, and applies the specified sort order.
 * The results are paginated and returned via a [PagingSource] that emits [ContentPreviewWithDetails] objects.
 *
 * It joins the `content` table with `article_attributes` and `content_tags` for filtering and projection,
 * while explicitly excluding records where `action = 'delete'`.
 *
 * @param query Encapsulates filters and sorting options for the content list.
 * @param contentDao DAO interface for accessing the content and related tables via a raw query.
 *
 * @return A [PagingSource] that emits paginated [ContentPreviewWithDetails] objects matching the query.
 *
 * ### SQL Query Construction Summary:
 * - Joins:
 *   - LEFT JOIN `content_tags` ON content.id = content_tags.contentId
 *   - LEFT JOIN `article_attributes` ON content.id = article_attributes.contentId
 * - Filters:
 *   - `content.action <> 'delete'`
 *   - Optional `content.type IN (...)` if `query.types` is not empty
 *   - Optional `content_tags.tagName IN (...)` if `query.tags.value` is not empty
 * - Sorting:
 *   - By article title (asc/desc)
 *   - Or by content update timestamp (asc/desc)
 *
 * ### Example Use Case:
 * ```
 * val pagingSource = contentItemPagingSource(
 *     query = Query(types = listOf("article"), tags = listOf("nutrition"), sortedBy = ByDateNewestFirst),
 *     contentDao = myDao
 * )
 * ```
 */
fun contentItemPagingSource(
    query: Query,
    contentDao: ContentDao
): PagingSource<Int, ContentPreviewWithDetails> {
    val sqlBuilder = StringBuilder()
    val args = mutableListOf<Any>()

    sqlBuilder.append(
        """
        SELECT DISTINCT content.*
        FROM content
        LEFT JOIN content_tags 
          ON  content.id = content_tags.contentId
        LEFT JOIN article_attributes
          ON content.id = article_attributes.contentId
        WHERE content.action <> 'delete'
    """.trimIndent()
    )

    // WHERE clauses
    val whereClauses = mutableListOf<String>()

    // Filter by type (if the list is non-empty)
    if (query.types.isNotEmpty()) {
        // For each type, we generate “?”
        val placeholders = query.types.joinToString(separator = ",") { "?" }
        whereClauses += "content.type IN ($placeholders)"
        args.addAll(query.types.map { it.toString() })
    }

    // Filter by tags (if the list is non-empty)
    if (query.tags.value.isNotEmpty()) {
        val placeholders = query.tags.value.joinToString(separator = ",") { "?" }
        whereClauses += "content_tags.tagName IN ($placeholders)"
        args.addAll(query.tags.value.map { it })
    }

    // Collect WHERE if there are conditions
    if (whereClauses.isNotEmpty()) {
        sqlBuilder.append("\nAND ")
        sqlBuilder.append(whereClauses.joinToString(" AND "))
    }

    // ORDER BY depending on the sorting option
    sqlBuilder.append("\nORDER BY ")
    when (query.sortedBy) {
        ContentItemsSortedType.ByNameAsc -> {
            sqlBuilder.append("article_attributes.title ASC")
        }

        ContentItemsSortedType.ByNameDesc -> {
            sqlBuilder.append("article_attributes.title DESC")
        }

        ContentItemsSortedType.ByDateNewestFirst -> {
            sqlBuilder.append("content.updatedAt DESC")
        }

        ContentItemsSortedType.ByDateOldestFirst -> {
            sqlBuilder.append("content.updatedAt ASC")
        }
    }

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


    return contentDao.getContent(rawQuery)
}
