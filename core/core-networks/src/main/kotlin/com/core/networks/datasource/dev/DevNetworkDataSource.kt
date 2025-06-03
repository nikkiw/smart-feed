package com.core.networks.datasource.dev

import com.core.networks.datasource.NetworkDataSource
import com.core.networks.models.ContentAttributes
import com.core.networks.models.ContentUpdate
import com.core.networks.models.UpdatesMeta
import com.core.networks.models.UpdatesResponse
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.plusAssign
import kotlin.random.Random

/**
 * Девелоперская реализация NetworkDataSource, которая возвращает фиктивные данные.
 * Используется для разработки, когда реального API ещё нет или вы хотите отлаживать UI без бэка.
 */
class DevNetworkDataSource : NetworkDataSource {

    // Хранение токена и последнего синка в памяти
    private val accessTokenRef = AtomicReference<String?>(null)
    private val lastSyncAtRef = AtomicReference<String>("1970-01-01T00:00:00Z")

    // Генерация «базы» из 100+ элементов при инициализации
    private val dummyData: List<ContentUpdate> by lazy { generateDummyUpdates(150) }
    // Здесь мы генерируем 150, чтобы явно быть больше 100

    override suspend fun getUpdates(
        since: String,
        limit: Int
    ): Result<UpdatesResponse> {
        // Эмулируем небольшую задержку, как будто идёт запрос по сети
        delay(200)

        // Находим индекс первого элемента, у которого updatedAt > since
        val filtered = dummyData.filter { it.updatedAt > since }
            .sortedBy{ it.updatedAt}

        // Берём нужный кусок, начиная с позиции start, размером limit (как минимум 100)
        val slice = filtered.take(limit.coerceAtLeast(100))

        // Формируем meta: nextSince = updatedAt последнего возвращённого элемента
        val nextSince = if (slice.isNotEmpty()) {
            slice.last().updatedAt
        } else {
            since
        }
        // hasMore = true, если после взятого среза остаются ещё элементы
        val hasMore = filtered.size > slice.size

        val response = UpdatesResponse(
            data = slice,
            meta = UpdatesMeta(
                nextSince = nextSince,
                hasMore = hasMore
            )
        )
        return Result.success(response)
    }

    override suspend fun getContentById(type: String, id: String): Result<ContentUpdate> {
        // Эмулируем задержку
        delay(100)

        val found = dummyData.find { it.id == id && it.type == type }
        return if (found != null) {
            Result.success(found)
        } else {
            Result.failure(NoSuchElementException("Content with id=$id and type=$type not found"))
        }
    }

    override fun getAccessToken(): String? {
        return accessTokenRef.get()
    }

    override fun saveAccessToken(token: String) {
        accessTokenRef.set(token)
    }

    override fun saveLastSyncAt(timestamp: String) {
        lastSyncAtRef.set(timestamp)
    }

    override fun getLastSyncAt(): String {
        return lastSyncAtRef.get()
    }

    // -----------------------
    // Вспомогательный метод для генерации «фейковых» данных
    // -----------------------

    private fun generateDummyUpdates(count: Int): List<ContentUpdate> {
        val list = mutableListOf<ContentUpdate>()
        val now = System.currentTimeMillis()
        for (i in 1..count) {
            // Чередуем типы: каждые 2 статьи — одна викторина
            val isQuiz = (i % 3 == 0)
            val type = if (isQuiz) "quiz" else "article"
            val id = "$type-$i"
            val action = if (Random.Default.nextBoolean()) "upsert" else "delete"
            // Форматируем updatedAt как ISO-строку (примерно)
            val updatedAt = isoTimestamp(now - i * 60_000L) // каждую минуту назад
            val url = "https://example.com/images/$type/$i.jpg"

            val attributes: ContentAttributes = if (type == "article") {
                ContentAttributes.Article(
                    title = "Заголовок статьи №$i",
                    content = "Содержимое статьи №$i. Здесь может быть любой текст.",
                    tags = listOf("тег1", "тег2", "пример")
                )
            } else {
                ContentAttributes.Quiz(
                    questions = listOf(
                        "Вопрос 1 для викторины №$i?",
                        "Вопрос 2 для викторины №$i?",
                        "Вопрос 3 для викторины №$i?"
                    )
                )
            }

            list += ContentUpdate(
                id = id,
                type = type,
                action = action,
                updatedAt = updatedAt,
                url = url,
                attributes = attributes
            )
        }
        // Отсортируем по updatedAt по убыванию (новые первыми)
        return list.sortedByDescending { it.updatedAt }
    }

    private fun isoTimestamp(epochMillis: Long): String {
        // Простейшая конверсия в ISO-like формат
        val date = Date(epochMillis)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }
}