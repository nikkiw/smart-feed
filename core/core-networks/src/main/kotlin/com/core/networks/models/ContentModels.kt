package com.core.networks.models

import androidx.annotation.Keep
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName


@Keep
data class Embeddings(
    @SerializedName("typeName") val typeName: String,
    @SerializedName("size") val size: Int,
    @SerializedName("data") val data: List<Double>
)

@Keep
sealed class ContentAttributes {
    @Keep
    data class Article(
        @SerializedName("title") val title: String,
        @SerializedName("shortDescription") val shortDescription: String,
        @SerializedName("content") val content: String,
        @SerializedName("embeddings") val embeddings: Embeddings
    ) : ContentAttributes()

    @Keep
    data class Quiz(
        @SerializedName("questions") val questions: List<String>
    ) : ContentAttributes()

    // При необходимости добавьте другие типы контента
}

@Keep
data class ContentUpdate(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("action") val action: String, // "upsert" или "delete"
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("mainImageUrl") val mainImageUrl: String,
    @SerializedName("tags") val tags: List<String>,
    @SerializedName("attributes") val attributes: ContentAttributes? // теперь полиморфный тип
)

@Keep
data class UpdatesMeta(
    @SerializedName("nextSince") val nextSince: String,
    @SerializedName("hasMore") val hasMore: Boolean
)

@Keep
data class UpdatesResponse(
    @SerializedName("data") val data: List<ContentUpdate>,
    @SerializedName("meta") val meta: UpdatesMeta
)


// Полный десериализатор для ContentUpdate, который читает поле "type" и создаёт соответствующий ContentAttributes
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
