package com.example.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.*

// -----------------------------
// 1. Models (analog of Android version)
// -----------------------------

@Serializable
sealed class ContentAttributes {
    @Serializable
    @SerialName("article")
    data class Article(
        @SerialName("title") val title: String,
        @SerialName("content") val content: String,
        @SerialName("tags") val tags: List<String>
    ) : ContentAttributes()

    @Serializable
    @SerialName("quiz")
    data class Quiz(
        @SerialName("questions") val questions: List<String>
    ) : ContentAttributes()
}

@Serializable
data class ContentUpdate(
    @SerialName("id") val id: String,
    @SerialName("type") val type: String,
    @SerialName("action") val action: String, // "upsert" или "delete"
    @SerialName("updatedAt") val updatedAt: String,
    @SerialName("mainImageUrl") val mainImageUrl: String,
    @SerialName("attributes") val attributes: ContentAttributes? = null
)

@Serializable
data class UpdatesMeta(
    @SerialName("nextSince") val nextSince: String,
    @SerialName("hasMore") val hasMore: Boolean
)

@Serializable
data class UpdatesResponse(
    @SerialName("data") val data: List<ContentUpdate>,
    @SerialName("meta") val meta: UpdatesMeta
)

// -----------------------------
// 2. Generation of a “base” of fictitious data
// -----------------------------

object DummyData {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    // List of 150 items (newest first)
    val allUpdates: List<ContentUpdate> by lazy { generateDummyUpdates(150) }

    private fun generateDummyUpdates(count: Int): List<ContentUpdate> {
        val nowMillis = System.currentTimeMillis()
        val list = mutableListOf<ContentUpdate>()

        for (i in 1..count) {
            val isQuiz = (i % 3 == 0)
            val type = if (isQuiz) "quiz" else "article"
            val id = "$type-$i"
            val action = if (Random().nextBoolean()) "upsert" else "delete"
            // Move the timestamp back i minutes.
            val ts = nowMillis - i * 60_000L
            val updatedAt = dateFormat.format(Date(ts))
            val mainImageUrl = "https://example.com/images/$type/$i.jpg"

            val attributes: ContentAttributes? =
                if (action == "upsert") {
                    if (type == "article") {
                        ContentAttributes.Article(
                            title = "Article title №$i",
                            content = "Content of article №$i. This can be any text.",
                            tags = listOf("tag1", "tag2", "example")
                        )
                    } else {
                        ContentAttributes.Quiz(
                            questions = listOf(
                                "Question 1 for the quiz №$i?",
                                "Question 2 for the quiz №$i?",
                                "Question 3 for the quiz №$i?"
                            )
                        )
                    }
                } else {
                    null
                }

            list += ContentUpdate(
                id = id,
                type = type,
                action = action,
                updatedAt = updatedAt,
                mainImageUrl = mainImageUrl,
                attributes = attributes
            )
        }
        // Sorting from oldest to newest
        return list.sortedBy { it.updatedAt }
    }
}

// -----------------------------
// 3. Function for filtering and cursor pagination
// -----------------------------

fun List<ContentUpdate>.pagedSince(since: String, limit: Int): Pair<List<ContentUpdate>, Boolean> {
    // Take only those records that have updatedAt > since
    val filtered = this.filter { it.updatedAt > since }
    // From the filtered ones we return the first limit elements
    val slice = filtered.take(limit)
    // If there are more items in the filtered list than taken, then there are more pages
    val hasMore = filtered.size > slice.size
    return Pair(slice, hasMore)
}

// -----------------------------
// 4. Entry point and Ktor module
// -----------------------------

fun main() {
    embeddedServer(Netty, watchPaths = listOf("com.example.server")) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    // Request logging
    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
    }
    // JSON serialization
    install(ContentNegotiation) {
        json()
    }

    routing {
        get("/updates") {
            val since = call.request.queryParameters["since"]
            if (since.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing 'since'"))
                return@get
            }
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100

            // Pagination by “numeric cursor”: take all records with updatedAt < since
            val (slice, hasMore) = DummyData.allUpdates.pagedSince(since, limit)

            // Следующий курсор — это updatedAt последней записи из текущей страницы,
            // либо тот же самый since, если страница пуста
            val nextSince = slice.lastOrNull()?.updatedAt ?: since

            val response = UpdatesResponse(
                data = slice,
                meta = UpdatesMeta(nextSince = nextSince, hasMore = hasMore)
            )
            call.respond(response)
        }

        get("/content/{type}/{id}") {
            val type = call.parameters["type"]
            val id = call.parameters["id"]
            if (type.isNullOrBlank() || id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing type or id"))
                return@get
            }
            val found = DummyData.allUpdates.find { it.type == type && it.id == id }
            if (found != null) {
                call.respond(found)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not found"))
            }
        }
    }
}
