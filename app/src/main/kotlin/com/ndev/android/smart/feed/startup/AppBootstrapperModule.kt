package com.ndev.android.smart.feed.startup

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBootstrapperModule {
    @Singleton
    @Binds
    abstract fun bindAppBootstrapper(appBootstrapperImpl: AppBootstrapperImpl): AppBootstrapper
}
