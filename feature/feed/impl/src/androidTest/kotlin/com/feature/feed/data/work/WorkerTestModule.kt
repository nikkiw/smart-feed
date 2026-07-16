package com.feature.feed.data.work

import android.content.Context
import android.widget.ImageView
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSyncContentUseCase
    @Inject
    constructor() : SyncContentUseCase {
        var shouldFail = false
        val invoked = AtomicBoolean(false)

        override suspend fun invoke(): Result<Unit> {
            invoked.set(true)
            return if (shouldFail) {
                Result.failure(Exception("Test failure"))
            } else {
                Result.success(Unit)
            }
        }
    }

@Singleton
class FakeImageLoader
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
