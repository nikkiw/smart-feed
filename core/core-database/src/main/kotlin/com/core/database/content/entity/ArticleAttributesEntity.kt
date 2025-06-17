package com.core.database.content.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Stores detailed attributes specific to article content.
 *
 * This entity complements [ContentEntity] and includes article-specific fields.
 *
 * @property contentId ID of the associated content (foreign key).
 * @property title Title of the article.
 * @property content Full body text of the article.
 * @property shortDescription Short summary of the article.
 * @property unitEmbedding Precomputed semantic embedding for similarity/recommendation purposes.
 */
@Entity(
    tableName = "article_attributes",
    foreignKeys = [ForeignKey(
        entity = ContentEntity::class,
        parentColumns = ["id"],
        childColumns = ["contentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ArticleAttributesEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val content: String,
    val shortDescription: String,
    val unitEmbedding: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ArticleAttributesEntity

        return contentId == other.contentId &&
                title == other.title &&
                content == other.content &&
                shortDescription == other.shortDescription
    }

    override fun hashCode(): Int {
        var result = contentId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + shortDescription.hashCode()
        return result
    }
}
