package com.core.domain.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@JvmInline
value class ContentItemId(val value: String)

@JvmInline
value class Title(val value: String)

@JvmInline
value class ShortDescription(val value: String)

@JvmInline
value class Content(val value: String)

@JvmInline
value class Tags(val value: List<String> = emptyList())

@JvmInline
value class ImageUrl(val value: String)

@JvmInline
value class Embeddings(val value: FloatArray)



/**
 * value class, который внутри хранит epochMillis (Long).
 * работает с Date/SimpleDateFormat.
 */
@JvmInline
value class UpdatedAt(val epochMillis: Long) {
    /**
     * Если нужен java.util.Date (работает на всех API) — возвращает Date.
     */
    fun toDate(): Date = Date(epochMillis)

    /**
     * Представить UpdatedAt в ISO-строке.
     * — форматируем через SimpleDateFormat.
     */
    override fun toString(): String {
        // Для простоты — выводим без миллисекунд, в формате UTC
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMillis))
    }

    companion object {
        /**
         * Создать UpdatedAt с «сейчас».
         */
        fun now(): UpdatedAt = UpdatedAt(System.currentTimeMillis())

        /**
         * Разбирает ISO-8601 строку (например, "2021-05-14T09:30:00Z" или "2021-05-14T09:30:00.123Z"
         * или со смещением "+03:00"), и возвращает UpdatedAt, внутри которогоepochMillis.
         *
         *  пробуем несколько шаблонов через SimpleDateFormat.
         *
         * @throws IllegalArgumentException если строка не подошла ни под один шаблон.
         */
        fun parse(isoString: String): UpdatedAt {
            val patterns = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            for (pat in patterns) {
                try {
                    val sdf = SimpleDateFormat(pat, Locale.US)
                    if (pat.endsWith("'Z'")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val date = sdf.parse(isoString)
                    if (date != null) {
                        return UpdatedAt(date.time)
                    }
                } catch (_: ParseException) {
                    // игнорируем, пробуем следующий шаблон
                }
            }
            throw IllegalArgumentException("Невозможно распарсить ISO-строку '$isoString'")
        }

    }
}


enum class ContentItemType(private val typeName: String) {
    ARTICLE("article"),
    UNKNOWN("unknown");

    override fun toString(): String = typeName

    companion object {
        fun fromString(type: String): ContentItemType = when (type.lowercase()) {
            "article" -> ARTICLE
            else -> UNKNOWN
        }
    }
}
