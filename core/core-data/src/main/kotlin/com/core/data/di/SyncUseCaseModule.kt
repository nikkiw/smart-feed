package com.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object SyncUseCaseModule {

    @Provides
    @Named("unique_work_name")
    fun provideUniqueWorkName(): String = "manual_sync_content"

    @Provides
    @Named("tag_work")
    fun provideTagWork(): String = "manual_sync_content"
}