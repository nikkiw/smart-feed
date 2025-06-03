package com.core.networks.datasource.prod


import com.core.networks.models.ContentUpdate
import com.core.networks.models.UpdatesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ContentApi {

    /**
     * Request example:
     * GET /updates?since=2025-05-01T00:00:00Z&limit=100&start=0
     * Return UpdatesResponse в JSON.
     */
    @GET("updates")
    suspend fun getUpdates(
        @Query("since") since: String,
        @Query("limit") limit: Int
    ): UpdatesResponse

    /**
     * Request example:
     * GET /content/{type}/{id}
     * Return single object ContentUpdate.
     */
    @GET("content/{type}/{id}")
    suspend fun getContentById(
        @Path("type") type: String,
        @Path("id") id: String
    ): ContentUpdate
}
