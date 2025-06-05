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
    @TypeConverters(TagsConverter::class)
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
    val content: String
)


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


// -- Converters for List<String> fields --
class TagsConverter {
    @TypeConverter
    fun fromTags(tags: List<String>?): String? = tags?.let { Gson().toJson(it) }

    @TypeConverter
    fun toTags(json: String?): List<String>? =
        json?.let { Gson().fromJson(it, object : TypeToken<List<String>>() {}.type) }
}
