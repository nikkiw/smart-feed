package com.core.data.di

import android.content.Context
import android.widget.ImageView
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreDataFakeImageLoader
    @Inject
    constructor() : ImageLoader {
        override fun load(
            context: Context,
            imageSource: ImageSource,
            imageView: ImageView,
            options: ImageOptions,
        ) = Unit

        override fun preload(
            context: Context,
            imageSource: ImageSource,
            options: ImageOptions,
        ) = Unit
    }
