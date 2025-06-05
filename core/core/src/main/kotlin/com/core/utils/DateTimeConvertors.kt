package com.core.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateTimeConvertors {
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

    fun parseIsoToLongMs(isoString: String): Long {
        return parseIsoToDate(isoString)?.time ?: 0
    }
}