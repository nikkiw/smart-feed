package com.core.database.content

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.ByteBuffer
import java.nio.ByteOrder

@Entity(
    tableName = "content_updates",
    indices = [
        Index(value = ["type"]),
        Index(value = ["updatedAt"])
    ]
)
data class ContentUpdateEntity(
    @PrimaryKey val id: String,
    val type: String,
    val action: String,
    val updatedAt: Long,
    val mainImageUrl: String,
    @TypeConverters(Converter::class)
    val tags: List<String>
)

@Entity(
    tableName = "article_attributes",
    foreignKeys = [ForeignKey(
        entity = ContentUpdateEntity::class,
        parentColumns = ["id"],
        childColumns = ["contentUpdateId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ArticleAttributesEntity(
    @PrimaryKey val contentUpdateId: String,
    val title: String,
    val content: String,
    val shortDescription: String,
    val embeddings: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleAttributesEntity

        if (contentUpdateId != other.contentUpdateId) return false
        if (title != other.title) return false
        if (content != other.content) return false
        if (shortDescription != other.shortDescription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentUpdateId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + shortDescription.hashCode()
        return result
    }
}


// Data class для получения данных с отношениями
data class ContentUpdateWithDetails(
    @Embedded val contentUpdate: ContentUpdateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "contentUpdateId"
    )
    val article: ArticleAttributesEntity?,
)


@Entity(
    tableName = "content_update_tags",
    primaryKeys = ["contentUpdateId", "tagName"],
    foreignKeys = [
        ForeignKey(
            entity = ContentUpdateEntity::class,
            parentColumns = ["id"],
            childColumns = ["contentUpdateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("contentUpdateId"),
        Index("tagName")
    ]
)
data class ContentUpdateTagCrossRef(
    val contentUpdateId: String,
    val tagName: String
)


data class ArticlePreviewAttributes(
    val contentUpdateId: String,
    val title: String,
    val shortDescription: String
)


data class ContentPreviewWithDetails(
    @Embedded val contentUpdate: ContentUpdateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "contentUpdateId",
        entity = ArticleAttributesEntity::class,
        projection = ["contentUpdateId", "title", "shortDescription"]
    )
    val article: ArticlePreviewAttributes?
)


class Converter {
    private val gson = Gson()

    // -- Converters for List<String> fields --
    @TypeConverter
    fun fromTags(tags: List<String>?): String? = tags?.let { gson.toJson(it) }

    @TypeConverter
    fun toTags(json: String?): List<String>? =
        json?.let { gson.fromJson(it, object : TypeToken<List<String>>() {}.type) }


    // --- Для FloatArray ---
//    @TypeConverter
//    fun fromEmbeddings(embeddings: FloatArray?): String? = embeddings?.let { gson.toJson(it) }
//
//    @TypeConverter
//    fun toEmbeddings(json: String?): FloatArray? =
//        json?.let { gson.fromJson(it, FloatArray::class.java) }

    @TypeConverter
    fun fromFloatArray(floats: FloatArray?): ByteArray? {
        if (floats == null) return null
        if (floats.isEmpty()) return ByteArray(0)

        return ByteBuffer.allocate(floats.size * Float.SIZE_BYTES)
            .order(ByteOrder.LITTLE_ENDIAN)
            .apply {
                floats.forEach(::putFloat)
            }
            .array()
    }

    @TypeConverter
    fun toFloatArray(bytes: ByteArray?): FloatArray? {
        if (bytes == null) return null
        if (bytes.isEmpty()) return FloatArray(0)

        require(bytes.size % Float.SIZE_BYTES == 0) {
            "ByteArray размер (${bytes.size}) должен быть кратен ${Float.SIZE_BYTES}"
        }

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        val floatCount = bytes.size / Float.SIZE_BYTES

        return FloatArray(floatCount) {
            buffer.getFloat()
        }
    }
}
