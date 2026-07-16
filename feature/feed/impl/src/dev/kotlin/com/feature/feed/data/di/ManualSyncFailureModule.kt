package com.feature.feed.data.di

import com.feature.feed.data.usecase.sync.ManualSyncFailurePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManualSyncFailureModule {
    @Provides
    @Singleton
    fun provideManualSyncFailurePolicy(): ManualSyncFailurePolicy {
        val attempts = AtomicInteger()
        return ManualSyncFailurePolicy {
            val attempt = attempts.incrementAndGet()
            if (attempt % FAILURE_INTERVAL == 0) {
                IOException("Dev refresh failure on attempt $attempt")
            } else {
                null
            }
        }
    }

    private const val FAILURE_INTERVAL = 3
}
