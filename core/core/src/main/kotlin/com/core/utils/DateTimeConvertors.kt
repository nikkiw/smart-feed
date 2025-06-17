package com.core.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Utility object for parsing ISO 8601 date-time strings into Java [Date] objects or epoch milliseconds.
 */
object DateTimeConvertors {

    /**
     * Parses an ISO 8601 date-time string into a [Date].
     *
     * Supports multiple ISO formats:
     * - `yyyy-MM-dd'T'HH:mm:ss`
     * - `yyyy-MM-dd'T'HH:mm:ss'Z'` (UTC)
     * - `yyyy-MM-dd'T'HH:mm:ss.SSS'Z'` (UTC with milliseconds)
     * - `yyyy-MM-dd'T'HH:mm:ssXXX` (with time zone offset)
     *
     * Tries each pattern in order and returns the first successful parse.
     *
     * @param isoString The ISO 8601 date-time string to parse.
     * @return A [Date] representing the parsed moment, or `null` if none of the patterns match.
     */
    fun parseIsoToDate(isoString: String): Date? {
            val patterns = arrayOf(
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            for (pat in patterns) {
                try {
                    val sdf = SimpleDateFormat(pat, Locale.getDefault())
                    if (pat.endsWith("'Z'")) {
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                    }
                    return sdf.parse(isoString)
                } catch (_: ParseException) {
                    // игнорируем, пробуем следующий шаблон
                }
            }
        return null
    }

    /**
     * Parses an ISO 8601 date-time string and returns its epoch time in milliseconds.
     *
     * @param isoString The ISO 8601 date-time string to parse.
     * @return The epoch milliseconds of the parsed date, or `0` if parsing fails.
     */
    fun parseIsoToLongMs(isoString: String): Long {
        return parseIsoToDate(isoString)?.time ?: 0
    }
}