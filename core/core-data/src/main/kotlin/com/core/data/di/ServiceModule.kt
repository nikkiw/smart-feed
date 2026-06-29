package com.core.data.di

import com.core.data.service.AppBootstrapperImpl
import com.core.domain.service.AppBootstrapper
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Singleton
    @Binds
    abstract fun bindAppBootstrapper(appBootstrapperImpl: AppBootstrapperImpl): AppBootstrapper
}
