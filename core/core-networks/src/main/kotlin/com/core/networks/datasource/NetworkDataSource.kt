package com.core.networks.datasource

import com.core.networks.models.ContentUpdate
import com.core.networks.models.UpdatesResponse

interface NetworkDataSource {
    suspend fun getUpdates(
        since: String,
        limit: Int = 100
    ): Result<UpdatesResponse>

    suspend fun getContentById(type: String, id: String): Result<ContentUpdate>

    fun getAccessToken(): String?
    fun saveAccessToken(token: String)

    fun saveLastSyncAt(timestamp: String)
    fun getLastSyncAt(): String
}