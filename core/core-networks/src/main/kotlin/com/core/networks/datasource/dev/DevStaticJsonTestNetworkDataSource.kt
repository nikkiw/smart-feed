package com.core.networks.datasource.dev

import android.content.Context
import com.core.networks.datasource.NetworkDataSource
import com.core.networks.models.ContentUpdate
import com.core.networks.models.UpdatesMeta
import com.core.networks.models.UpdatesResponse
import com.core.networks.models.contentUpdateDeserializer
import com.google.gson.GsonBuilder
import java.io.IOException
import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

class DevStaticJsonTestNetworkDataSource(
    private val context: Context
) : NetworkDataSource {

    private val accessTokenRef = AtomicReference<String?>(null)
    private val lastSyncAtRef = AtomicReference<String>("1970-01-01T00:00:00Z")

    private var counterGetUpdates = 0

    private val gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(ContentUpdate::class.java, contentUpdateDeserializer)
            .create()
    }

    override suspend fun getUpdates(
        since: String,
        limit: Int
    ): Result<UpdatesResponse> {
        return runCatching {
            if (++counterGetUpdates % 3 == 0){
                throw Exception("Test exception counterGetUpdates=$counterGetUpdates")
            }
            val jsonString = readJsonFromAssets("articles.json")
            val response = gson.fromJson(jsonString, UpdatesResponse::class.java)

            // Фильтруем данные по времени since
            val filteredData = if (since.isNotEmpty()) {
                response.data.filter { article ->
                    isAfterTimestamp(article.updatedAt, since)
                }
            } else {
                response.data
            }

            // Применяем лимит
            val limitedData = filteredData.take(limit)

            // Создаем мета-информацию
            val hasMore = filteredData.size > limit
            val nextSince = if (limitedData.isNotEmpty()) {
                limitedData.maxByOrNull { it.updatedAt }?.updatedAt ?: since
            } else {
                since
            }

            val filteredResponse = UpdatesResponse(
                data = limitedData,
                meta = UpdatesMeta(
                    nextSince = nextSince,
                    hasMore = hasMore
                )
            )

            filteredResponse
        }
    }

    override suspend fun getContentById(type: String, id: String): Result<ContentUpdate> {
        return runCatching {
            val jsonString = readJsonFromAssets("articles.json")
            val response = gson.fromJson(jsonString, UpdatesResponse::class.java)

            val content = response.data.find { it.id == id && it.type == type }

            content ?: throw Exception("Content not found with id: $id and type: $type")
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

    private fun readJsonFromAssets(fileName: String): String {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            throw IOException("Could not read file: $fileName", e)
        }
    }

    private fun isAfterTimestamp(timestamp1: String, timestamp2: String): Boolean {
        return if (timestamp2.isEmpty()) {
            true
        } else {
            try {
                val instant1 = Instant.parse(timestamp1)
                val instant2 = Instant.parse(timestamp2)
                instant1.isAfter(instant2)
            } catch (e: Exception) {
                true // В случае ошибки парсинга возвращаем true
            }
        }
    }
}