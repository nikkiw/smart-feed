package com.image.glide.di

import com.core.image.ImageLoader
import com.image.glide.GlideImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ImageModule {

    @Singleton
    @Provides
    fun funImageLoader(): ImageLoader {
        return GlideImageLoader()
    }
}