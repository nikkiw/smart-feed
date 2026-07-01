package com.feature.recommendation.data.di

import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.feature.feed.local.content.ContentDao
import com.feature.recommendation.data.repository.RecommendationRepositoryImpl
import com.feature.recommendation.data.service.RecommenderImpl
import com.feature.recommendation.data.usecase.RecommendForArticleUseCaseImpl
import com.feature.recommendation.data.usecase.RecommendForUserUseCaseImpl
import com.feature.recommendation.domain.repository.RecommendationRepository
import com.feature.recommendation.domain.service.Recommender
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import com.feature.recommendation.domain.usecase.RecommendForUserUseCase
import com.feature.recommendation.local.embedding.ArticleEmbeddingDao
import com.feature.recommendation.local.event.ContentInteractionStatsDao
import com.feature.recommendation.local.recommendation.RecommendationDao
import com.feature.userprofile.domain.repository.UserProfileRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RecommendationDataModule {
    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(repository: RecommendationRepositoryImpl): RecommendationRepository

    @Binds
    @Singleton
    abstract fun bindRecommendForArticleUseCase(
        recommendForArticleUseCaseImpl: RecommendForArticleUseCaseImpl,
    ): RecommendForArticleUseCase

    @Binds
    @Singleton
    abstract fun bindRecommendForUserUseCase(
        recommendForUserUseCaseImpl: RecommendForUserUseCaseImpl,
    ): RecommendForUserUseCase

    companion object {
        @Provides
        @Singleton
        @Suppress("LongParameterList")
        fun provideRecommender(
            userProfileRepository: UserProfileRepository,
            contentInteractionStatsDao: ContentInteractionStatsDao,
            contentDao: ContentDao,
            articleEmbeddingDao: ArticleEmbeddingDao,
            recommendationDao: RecommendationDao,
            @IoDispatcher ioDispatcher: CoroutineDispatcher,
            @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
            @ApplicationScope applicationScope: CoroutineScope,
        ): Recommender {
            return RecommenderImpl(
                userProfileRepository = userProfileRepository,
                contentInteractionStatsDao = contentInteractionStatsDao,
                contentDao = contentDao,
                articleEmbeddingDao = articleEmbeddingDao,
                recommendationDao = recommendationDao,
                ioDispatcher = ioDispatcher,
                defaultDispatcher = defaultDispatcher,
                applicationScope = applicationScope,
                topK = 10,
                coldK = 4,
                mmrK = 5,
                lambda = 0.5f,
            )
        }
    }
}
