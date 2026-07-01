package com.core.analytics.impl.di

import com.core.analytics.api.AnalyticsService
import com.core.analytics.impl.AnalyticsServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    @Binds
    @Singleton
    abstract fun bindAnalyticsService(impl: AnalyticsServiceImpl): AnalyticsService
}
