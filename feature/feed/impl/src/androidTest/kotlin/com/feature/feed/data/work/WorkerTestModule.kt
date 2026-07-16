package com.feature.feed.data.work

import android.content.Context
import android.widget.ImageView
import com.core.content.model.ContentId
import com.core.content.model.Tags
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.domain.model.ContentItem
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.recommendation.domain.service.Recommender
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeContentRepo
    @Inject
    constructor() : ContentItemRepository {
        var shouldFail = false

        override suspend fun getContentById(itemId: ContentId): Result<ContentItem> {
            error("Not used by ContentFetchWorker tests")
        }

        override suspend fun isEmpty(): Boolean = true

        override fun observeHasContent(): Flow<Boolean> = flowOf(false)

        override fun flowAllTags(): Flow<Tags> = flowOf(Tags(emptyList()))

        override suspend fun syncContent(): Result<Unit> =
            if (shouldFail) {
                Result.failure(Exception("Test failure"))
            } else {
                Result.success(Unit)
            }
    }

@Singleton
class FakeRecommender
    @Inject
    constructor() : Recommender {
        val updatedUser = AtomicBoolean(false)
        val updatedArticles = AtomicBoolean(false)

        override suspend fun updateRecommendationsForUser() {
            updatedUser.set(true)
        }

        override suspend fun updateRecommendationsForArticles() {
            updatedArticles.set(true)
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
