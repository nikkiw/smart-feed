package com.core.networks.datasource.dev

import com.core.networks.datasource.NetworkDataSource
import com.core.networks.models.ContentAttributes
import com.core.networks.models.ContentUpdate
import com.core.networks.models.Embeddings
import com.core.networks.models.UpdatesMeta
import com.core.networks.models.UpdatesResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.atomic.AtomicReference
import kotlin.random.Random

/**
 * Developer implementation of [NetworkDataSource] that returns mock data.
 * Used during development when the real API is not yet available
 * or when you want to debug the UI without a backend.
 */
class DevNetworkDataSource : NetworkDataSource {

    //  Storing token and last sync in memory
    private val accessTokenRef = AtomicReference<String?>(null)
    private val lastSyncAtRef = AtomicReference<String>("1970-01-01T00:00:00Z")
    val allTags = listOf(
        "technology",
        "health",
        "finance",
        "education",
        "environment"
    )

    // Generating a “base” of 100+ elements when initializing
    // Here we generate 150 to be obviously greater than 100
    val dummyData: List<ContentUpdate> by lazy { generateDummyUpdates(150) }

    override suspend fun getUpdates(
        since: String,
        limit: Int
    ): Result<UpdatesResponse> {

        val filtered = dummyData.filter { it.updatedAt > since }
            .sortedBy { it.updatedAt }

        val slice = filtered.take(limit)

        val nextSince = if (slice.isNotEmpty()) {
            slice.last().updatedAt
        } else {
            since
        }
        val hasMore = filtered.size > slice.size

        val response = UpdatesResponse(
            data = slice,
            meta = UpdatesMeta(
                nextSince = nextSince,
                hasMore = hasMore
            )
        )
        return Result.success(response)
    }

    override suspend fun getContentById(type: String, id: String): Result<ContentUpdate> {
        val found = dummyData.find { it.id == id && it.type == type }
        return if (found != null) {
            Result.success(found)
        } else {
            Result.failure(NoSuchElementException("Content with id=$id and type=$type not found"))
        }
    }

    override fun getAccessToken(): String? {
        return accessTokenRef.get()
    }

    override fun saveAccessToken(token: String) {
        accessTokenRef.set(token)
    }

    override fun saveLastSyncAt(timestamp: String) {
        lastSyncAtRef.set(timestamp)
    }

    override fun getLastSyncAt(): String {
        return lastSyncAtRef.get()
    }

    /**
     * Generates a list of mock [ContentUpdate] items for development and testing.
     *
     * @param count The number of dummy updates to generate.
     * @return A [List] of [ContentUpdate] instances with placeholder IDs, types, and timestamps.
     */
    private fun generateDummyUpdates(count: Int): List<ContentUpdate> {
        val list = mutableListOf<ContentUpdate>()
        val now = System.currentTimeMillis()
        for (i in 1..count) {
            // Alternate types: every 2 articles - one quiz
            val isQuiz = (i % 3 == 0)
            val type = if (isQuiz) "quiz" else "article"
            val id = "$type-$i"
            val action = if (Random.Default.nextBoolean()) "upsert" else "delete"
            // Format the updatedAt as an ISO string (roughly)
            val updatedAt = isoTimestamp(now - i * 60_000L) // every minute back
            val url = "https://picsum.photos/200"

            val attributes: ContentAttributes = if (type == "article") {
                ContentAttributes.Article(
                    title = "Article title №$i",
                    shortDescription = "Article Summary №$i",
                    content = "Content of article №$i. This can be any text.Short content of the article.",
                    embeddings = Embeddings(
                        typeName = "test",
                        size = 20,
                        data = List(20) { Random.nextDouble(-1.0, 1.0) }
                    )
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

            val tagCount = (1..allTags.size).random()        // random number between 1 and 5
            val randomTags = allTags.shuffled().take(tagCount)

            list += ContentUpdate(
                id = id,
                type = type,
                action = action,
                updatedAt = updatedAt,
                mainImageUrl = url,
                tags = randomTags,
                attributes = attributes
            )
        }
        // Sort by updatedAt in descending order (new first)
        return list.sortedByDescending { it.updatedAt }
    }

    /**
     * Converts the given epoch milliseconds to an ISO 8601 UTC timestamp string.
     *
     * Uses the pattern `yyyy-MM-dd'T'HH:mm:ss'Z'` and sets the time zone to UTC.
     *
     * @param epochMillis The time in milliseconds since the Unix epoch.
     * @return A formatted ISO 8601 string in UTC (e.g., `2024-06-01T09:15:00Z`).
     */
    private fun isoTimestamp(epochMillis: Long): String {
        val date = Date(epochMillis)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(date)
    }
}