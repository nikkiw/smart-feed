package com.core.data.work

import android.content.Context
import android.widget.ImageView
import androidx.paging.PagingData
import com.core.data.di.DataModule
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemPreview
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.Query
import com.core.domain.service.Recommender
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton


//@Module
//@TestInstallIn(
//    components = [SingletonComponent::class],
//    replaces = [
//        DataModule::class
//    ]
//)
//abstract class WorkerTestModule {
//    @Binds
//    @Singleton
//    abstract fun bindContentRepo(repo: FakeContentRepo): ContentItemRepository
//
//    @Binds
//    @Singleton
//    abstract fun bindRecommender(rec: FakeRecommender): Recommender
//
//
//    @Binds
//    @Singleton
//    abstract fun bindImageLoader(rec: FakeImageLoader): ImageLoader
//}


@Singleton
class FakeContentRepo @Inject constructor(): ContentItemRepository {
    // переключаемое поведение
    var shouldFail = false

    override fun flowContent(query: Query): Flow<PagingData<ContentItemPreview>> {
        TODO("Not yet implemented")
    }

    override suspend fun getContentById(itemId: ContentId): Result<ContentItem> {
        TODO("Not yet implemented")
    }

    override suspend fun isEmpty(): Boolean {
        TODO("Not yet implemented")
    }

    override fun flowAllTags(): Flow<Tags> {
        TODO("Not yet implemented")
    }

    override suspend fun syncContent(): Result<Unit> {
        return if (shouldFail) Result.failure(Exception("Test failure"))
        else Result.success(Unit)
    }
}

@Singleton
class FakeRecommender @Inject constructor(): Recommender {
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
class FakeImageLoader @Inject constructor(): ImageLoader{
    override fun load(
        context: Context,
        imageSource: ImageSource,
        imageView: ImageView,
        options: ImageOptions
    ) {
        TODO("Not yet implemented")
    }

    override fun preload(
        context: Context,
        imageSource: ImageSource,
        options: ImageOptions
    ) {
        TODO("Not yet implemented")
    }

}