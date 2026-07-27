package com.core.content.model

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Locale.ROOT
import java.util.TimeZone

/**
 * Unique identifier for content items.
 *
 * @property value Internal string representation of the content ID.
 */
@JvmInline
value class ContentId(val value: String)

/**
 * Represents the title of a content item.
 *
 * @property value Internal string value of the title.
 */
@JvmInline
value class Title(val value: String)

/**
 * Represents a short description or summary of content.
 *
 * @property value Internal string value of the short description.
 */
@JvmInline
value class ShortDescription(val value: String)

/**
 * Represents the full textual content of an article.
 *
 * @property value Internal string value containing the full article text.
 */
@JvmInline
value class Content(val value: String)

/**
 * Represents a list of tags associated with a content item.
 *
 * @property value Internal list of strings representing tags.
 */
@JvmInline
value class Tags(val value: List<String> = emptyList())

/**
 * Represents the URL to an image used as the main image of the content.
 *
 * @property value Internal string URL pointing to the image.
 */
@JvmInline
value class ImageUrl(val value: String)

/**
 * Language tag associated with content.
 *
 * Uses a normalized lowercase BCP-47 style code such as `en`, `ru`, or `und`
 * when the language is not determined.
 */
@JvmInline
value class ContentLanguage(val code: String) {
    companion object {
        val UNDETERMINED: ContentLanguage = ContentLanguage("und")
        val ENGLISH: ContentLanguage = ContentLanguage("en")
        val RUSSIAN: ContentLanguage = ContentLanguage("ru")

        fun from(raw: String?): ContentLanguage {
            val normalized = raw?.trim()?.lowercase(ROOT)
            return if (normalized.isNullOrEmpty()) UNDETERMINED else ContentLanguage(normalized)
        }

        fun resolve(explicitCode: String?, title: String, shortDescription: String, content: String): ContentLanguage {
            val explicitLanguage = from(explicitCode)
            return if (explicitLanguage != UNDETERMINED) {
                explicitLanguage
            } else {
                detectFromText(
                    buildString {
                        append(title)
                        append(' ')
                        append(shortDescription)
                        append(' ')
                        append(content)
                    },
                )
            }
        }

        fun detectFromText(text: String): ContentLanguage {
            var cyrillicLetters = 0
            var latinLetters = 0

            text.forEach { char ->
                when {
                    char in '\u0400'..'\u04FF' -> cyrillicLetters++
                    char.isLetter() && char.lowercaseChar() in 'a'..'z' -> latinLetters++
                }
            }

            return when {
                cyrillicLetters > latinLetters && cyrillicLetters > 0 -> RUSSIAN
                latinLetters > 0 -> ENGLISH
                else -> UNDETERMINED
            }
        }
    }
}

/**
 * Value class representing a timestamp of when content was last updated.
 *
 * Internally stores time as epoch milliseconds (`Long`), and provides utilities
 * for converting to [Date], parsing from ISO strings, and formatting to ISO strings.
 *
 * @property epochMillis Unix timestamp in milliseconds.
 */
@JvmInline
value class UpdatedAt(val epochMillis: Long) {
    /**
     * Converts this [UpdatedAt] into a [java.util.Date].
     *
     * @return A Date instance based on the stored timestamp.
     */
    fun toDate(): Date = Date(epochMillis)

    /**
     * Formats the timestamp into an ISO 8601-compliant string in UTC format.
     *
     * Example output: `"2024-05-20T12:34:56Z"`
     *
     * @return Formatted date-time string.
     */
    override fun toString(): String {
        // Для простоты — выводим без миллисекунд, в формате UTC
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date(epochMillis))
    }

    companion object {
        /**
         * Returns a new [UpdatedAt] instance set to the current time.
         *
         * @return Current timestamp.
         */
        fun now(): UpdatedAt = UpdatedAt(System.currentTimeMillis())

        /**
         * Parses an ISO 8601 date-time string into an [UpdatedAt].
         *
         * Supported formats:
         * - `yyyy-MM-dd'T'HH:mm:ss'Z'`
         * - `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'`
         * - `yyyy-MM-dd'T'HH:mm:ssXXX`
         * - `yyyy-MM-dd'T'HH:mm:ss`
         *
         * @param isoString ISO 8601 formatted date-time string.
         * @return A new [UpdatedAt] instance.
         * @throws IllegalArgumentException If the input string cannot be parsed.
         */
        fun parse(isoString: String): UpdatedAt {
            val patterns =
                arrayOf(
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ssXXX",
                    "yyyy-MM-dd'T'HH:mm:ss",
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

/**
 * Enum representing types of content available in the system.
 *
 * @property typeName Human-readable name of the content type.
 */
enum class ContentType(private val typeName: String) {
    ARTICLE("article"),
    UNKNOWN("unknown"),
    ;

    override fun toString(): String = typeName

    companion object {
        fun fromString(type: String): ContentType = when (type.lowercase()) {
            "article" -> ARTICLE
            else -> UNKNOWN
        }
    }
}
