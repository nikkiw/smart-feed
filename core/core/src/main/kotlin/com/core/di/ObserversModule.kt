package com.core.di

import android.content.Context
import com.core.observers.AppLifecycleObserver
import com.core.observers.ConnectivityRepository
import com.core.observers.ConnectivityRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationLifecycleObserver

@Module
@InstallIn(SingletonComponent::class)
object ObserversModule {

    @Provides
    @Singleton
    @ApplicationLifecycleObserver
    fun provideAppLifecycleObserver(
        @ApplicationScope applicationScope: CoroutineScope,
        @MainDispatcher mainDispatcher: CoroutineDispatcher
    ): AppLifecycleObserver {
        return AppLifecycleObserver(
            applicationScope,
            mainDispatcher
        )
    }

    @Provides
    @Singleton
    fun provideConnectivityRepository(
        @ApplicationContext context: Context,
        @ApplicationLifecycleObserver appLifecycleObserver: AppLifecycleObserver
    ): ConnectivityRepository {
        return ConnectivityRepositoryImpl(context).also {
            appLifecycleObserver.addObserver(it)
        }
    }
}