package com.core.domain.service

/**
 * Interface for application bootstrapping logic.
 * Handles the initial setup, syncing data, and scheduling background tasks on app launch.
 */
interface AppBootstrapper {
    /**
     * Initializes and bootstraps the application startup tasks.
     */
    fun bootstrap()
}
