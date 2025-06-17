package com.core.database.content.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Room type converters to handle non-primitive fields in entities.
 *
 * Converts between:
 * - [List]<[String]> and JSON
 * - [FloatArray] and [ByteArray] using little-endian encoding
 */
class Converter {
    private val gson = Gson()

    /**
     * Serializes a list of tags to JSON string.
     */
    @TypeConverter
    fun fromTags(tags: List<String>?): String? = tags?.let { gson.toJson(it) }

    /**
     * Deserializes a JSON string back into a list of tags.
     */
    @TypeConverter
    fun toTags(json: String?): List<String>? =
        json?.let { gson.fromJson(it, object : TypeToken<List<String>>() {}.type) }

    /**
     * Converts a [FloatArray] to a [ByteArray] using little-endian byte order.
     */
    @TypeConverter
    fun fromFloatArray(floats: FloatArray?): ByteArray? {
        if (floats == null) return null
        if (floats.isEmpty()) return ByteArray(0)

        return ByteBuffer.allocate(floats.size * Float.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply { floats.forEach(::putFloat) }
            .array()
    }

    /**
     * Converts a [ByteArray] back into a [FloatArray] using little-endian byte order.
     *
     * @throws IllegalArgumentException if the byte array size is invalid.
     */
    @TypeConverter
    fun toFloatArray(bytes: ByteArray?): FloatArray? {
        if (bytes == null) return null
        if (bytes.isEmpty()) return FloatArray(0)

        require(bytes.size % Float.SIZE_BYTES == 0) {
            "ByteArray size (${bytes.size}) must be a multiple of ${Float.SIZE_BYTES}"
        }

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floatCount = bytes.size / Float.SIZE_BYTES

        return FloatArray(floatCount) { buffer.getFloat() }
    }
}
