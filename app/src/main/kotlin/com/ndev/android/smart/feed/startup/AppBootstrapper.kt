package com.ndev.android.smart.feed.startup

/**
 * Interface for application bootstrapping logic.
 * Handles the initial setup, syncing data, and scheduling background tasks on app launch.
 */
interface AppBootstrapper {
    /**
     * Initializes and bootstraps the application startup tasks.
     */
    suspend fun bootstrap()
}
