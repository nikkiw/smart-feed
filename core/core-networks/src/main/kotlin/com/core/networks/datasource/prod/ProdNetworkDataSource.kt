package com.core.networks.datasource.prod


import android.content.SharedPreferences
import androidx.core.content.edit
import com.core.di.IoDispatcher
import com.core.networks.datasource.NetworkDataSource
import com.core.networks.models.ContentUpdate
import com.core.networks.models.UpdatesResponse
import com.core.runSuspendCatching
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProdNetworkDataSource @Inject constructor(
    private val api: ContentApi,
    private val prefs: SharedPreferences,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher
) : NetworkDataSource {

    companion object {
        private const val PREF_KEY_ACCESS_TOKEN = "pref_access_token"
        private const val PREF_KEY_LAST_SYNC_AT = "pref_last_sync_at"
    }

    override suspend fun getUpdates(
        since: String,
        limit: Int,
    ): Result<UpdatesResponse> = withContext(ioDispatcher) {
        runSuspendCatching {
            api.getUpdates(since = since, limit = limit)
        }
    }

    override suspend fun getContentById(type: String, id: String): Result<ContentUpdate> =
        withContext(ioDispatcher) {
            runSuspendCatching { api.getContentById(type = type, id = id) }
        }

    override fun getAccessToken(): String? {
        return prefs.getString(PREF_KEY_ACCESS_TOKEN, null)
    }

    override fun saveAccessToken(token: String) {
        prefs.edit { putString(PREF_KEY_ACCESS_TOKEN, token) }
    }

    override fun saveLastSyncAt(timestamp: String) {
        prefs.edit { putString(PREF_KEY_LAST_SYNC_AT, timestamp) }
    }

    override fun getLastSyncAt(): String {
        return prefs.getString(PREF_KEY_LAST_SYNC_AT, "1970-01-01T00:00:00Z")
            ?: "1970-01-01T00:00:00Z"
    }
}
