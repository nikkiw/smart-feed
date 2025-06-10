package com.core.networks.di

import android.content.Context
import com.core.networks.datasource.NetworkDataSource
import com.core.networks.datasource.dev.DevStaticJsonTestNetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkDataSource(
        @ApplicationContext context: Context
    ): NetworkDataSource {
        return DevStaticJsonTestNetworkDataSource(context)
    }

}