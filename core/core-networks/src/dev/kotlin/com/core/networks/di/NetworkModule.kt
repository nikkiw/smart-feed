package com.core.networks.di

import com.core.networks.datasource.dev.DevNetworkDataSource
import com.core.networks.datasource.NetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkDataSource(): NetworkDataSource {
        return DevNetworkDataSource()
    }

}