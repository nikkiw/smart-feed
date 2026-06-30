package com.feature.feed.data.di

import com.feature.feed.data.work.WorkerScheduleConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkConfigModule {
    @Singleton
    @Provides
    fun provideWorkerScheduleConfig(): WorkerScheduleConfig {
        return object : WorkerScheduleConfig {
            override val fetchInterval: Duration = Duration.ofMillis(TimeUnit.HOURS.toMillis(12))
            override val fetchFlex: Duration = Duration.ofMillis(TimeUnit.HOURS.toMillis(1))
        }
    }
}
