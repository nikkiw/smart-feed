package com.core.networks.datasource

import com.core.networks.models.ContentUpdate
import com.core.networks.models.UpdatesResponse

/**
 * Defines the contract for fetching and persisting network‑sourced data and synchronization metadata.
 *
 * All timestamps are passed in UTC ISO8601 format with a trailing “Z”:
 * `yyyy-MM-dd'T'HH:mm:ss'Z'` (e.g., `2024-06-01T09:15:00Z`).
 */
interface NetworkDataSource {

    /**
     * Retrieves a batch of updates from the server starting from a given timestamp.
     *
     * @param since ISO8601 UTC timestamp string indicating the point in time
     *              from which to fetch new updates (e.g., `2024-06-01T09:15:00Z`).
     * @param limit Maximum number of update records to retrieve. Defaults to 100.
     * @return A [Result] wrapping [UpdatesResponse] on success or an error on failure.
     */
    suspend fun getUpdates(
        since: String,
        limit: Int = 100
    ): Result<UpdatesResponse>

    /**
     * Fetches a specific content update by its type and identifier.
     *
     * @param type The category or collection name of the content.
     * @param id   The unique identifier of the content item.
     * @return A [Result] wrapping the [ContentUpdate] on success or an error on failure.
     */
    suspend fun getContentById(type: String, id: String): Result<ContentUpdate>

    /**
     * Returns the currently stored access token used for authenticated requests.
     *
     * @return The access token as a [String], or `null` if no token is saved.
     */
    fun getAccessToken(): String?

    /**
     * Persists a new access token for future authenticated requests.
     *
     * @param token The access token string to save.
     */
    fun saveAccessToken(token: String)

    /**
     * Saves the timestamp of the last successful synchronization.
     *
     * @param timestamp ISO8601 UTC timestamp string (e.g., `2024-06-01T09:15:00Z`).
     */
    fun saveLastSyncAt(timestamp: String)

    /**
     * Retrieves the timestamp of the last successful synchronization.
     *
     * @return An ISO8601 UTC timestamp string (e.g., `2024-06-01T09:15:00Z`),
     * or an empty string if never synced.
     */
    fun getLastSyncAt(): String
}
