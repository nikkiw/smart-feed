package com.ndev.android.smart.feed.startup

import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object AppStartupModule {
    @Provides
    fun provideStartupErrorReporter(): StartupErrorReporter =
        StartupErrorReporter { error ->
            Log.e(TAG, "Application startup failed", error)
        }

    private const val TAG = "AppStartup"
}
