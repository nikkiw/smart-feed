package com.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object SchedulerModule {

    @Provides
    @Named("workName")
    fun provideWorkName(): String = "ContentFetchWork"
}