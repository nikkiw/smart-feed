package com.feature.feed.data.di

import com.feature.feed.data.usecase.sync.ManualSyncFailurePolicy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ManualSyncFailureModule {
    @Provides
    fun provideManualSyncFailurePolicy(): ManualSyncFailurePolicy = ManualSyncFailurePolicy { null }
}
