package com.core.networks.models

import androidx.annotation.Keep
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName


/**
 * Represents vector embeddings associated with content, used for similarity search.
 *
 * @property typeName The descriptive name of the embedding type (e.g., "article", "quiz").
 * @property size The dimensionality of the embedding vector.
 * @property data The list of embedding values.
 */
@Keep
data class Embeddings(
    @SerializedName("typeName") val typeName: String,
    @SerializedName("size") val size: Int,
    @SerializedName("data") val data: List<Double>
)

/**
 * Sealed hierarchy of content-specific attributes.
 * Extend this when adding new content types.
 */
@Keep
sealed class ContentAttributes {

    /**
     * Attributes for an article content update.
     *
     * @property title The title of the article.
     * @property shortDescription A brief summary of the article.
     * @property content The full article text.
     * @property embeddings Precomputed embeddings for the article.
     */
    @Keep
    data class Article(
        @SerializedName("title") val title: String,
        @SerializedName("shortDescription") val shortDescription: String,
        @SerializedName("content") val content: String,
        @SerializedName("embeddings") val embeddings: Embeddings
    ) : ContentAttributes()

    /**
     * Attributes for a quiz content update.
     *
     * @property questions The list of quiz questions.
     */
    @Keep
    data class Quiz(
        @SerializedName("questions") val questions: List<String>
    ) : ContentAttributes()

}

/**
 * Represents a single content update from the server, including metadata
 * and polymorphic attributes.
 *
 * @property id Unique identifier of the content.
 * @property type The content type (e.g., "article", "quiz").
 * @property action The update action: `"upsert"` or `"delete"`.
 * @property updatedAt ISO 8601 UTC timestamp when the update occurred.
 * @property mainImageUrl URL to the main image for this content.
 * @property tags List of associated tags or categories.
 * @property attributes Polymorphic content-specific attributes, or `null` if none.
 */
@Keep
data class ContentUpdate(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("action") val action: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("mainImageUrl") val mainImageUrl: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("attributes") val attributes: ContentAttributes?
)

/**
 * Metadata for a batch of updates, indicating pagination state.
 *
 * @property nextSince ISO 8601 UTC timestamp to use for the next `since` parameter.
 * @property hasMore `true` if more updates are available beyond this batch.
 */
@Keep
data class UpdatesMeta(
    @SerializedName("nextSince") val nextSince: String,
    @SerializedName("hasMore") val hasMore: Boolean
)


/**
 * Response wrapper for a list of content updates and associated metadata.
 *
 * @property data The list of [ContentUpdate] items.
 * @property meta Pagination metadata for the update batch.
 */
@Keep
data class UpdatesResponse(
    @SerializedName("data") val data: List<ContentUpdate>,
    @SerializedName("meta") val meta: UpdatesMeta
)


/**
 * Custom Gson deserializer for [ContentUpdate], choosing the correct
 * [ContentAttributes] subtype based on the `type` field.
 */
val contentUpdateDeserializer = JsonDeserializer { json: JsonElement, typeOfT, context ->
    val obj = json.asJsonObject
    val id = obj.get("id").asString
    val type = obj.get("type").asString
    val action = obj.get("action").asString
    val updatedAt = obj.get("updatedAt").asString
    val mainImageUrl = obj.get("mainImageUrl").asString
    val tags = obj.getAsJsonArray("tags").map { it.asString }
    val attributesElement = if (obj.has("attributes") && !obj.get("attributes").isJsonNull)
        obj.get("attributes") else null

    val attributes: ContentAttributes? = when (type) {
        "article" -> attributesElement?.let {
            context.deserialize(it, ContentAttributes.Article::class.java)
        }

        "quiz" -> attributesElement?.let {
            context.deserialize(it, ContentAttributes.Quiz::class.java)
        }

        else -> null
    }

    ContentUpdate(id, type, action, updatedAt, mainImageUrl, tags, attributes)
}
