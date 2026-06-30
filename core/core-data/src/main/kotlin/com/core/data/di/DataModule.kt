package com.core.data.di

import com.core.data.repository.ContentItemRepositoryImpl
import com.core.data.repository.RecommendationRepositoryImpl
import com.core.data.repository.UserProfileRepositoryImpl
import com.core.data.service.AnalyticsServiceImpl
import com.core.data.service.RecommenderImpl
import com.feature.feed.local.content.ContentDao
import com.feature.feed.local.content.ContentTagsDao
import com.feature.feed.local.content.UpdatesMetaDao
import com.core.database.embeding.ArticleEmbeddingDao
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.event.EventLogDao
import com.core.database.recommendation.RecommendationDao
import com.core.database.userprofile.UserProfileDao
import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.recommendation.domain.repository.RecommendationRepository
import com.core.domain.repository.UserProfileRepository
import com.core.domain.service.AnalyticsService
import com.feature.recommendation.domain.service.Recommender
import com.core.networks.datasource.NetworkDataSource
import com.core.paging.ContentPagingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideContentItemRepositoryImpl(
        contentDao: ContentDao,
        contentTagsDao: ContentTagsDao,
        updatesMetaDao: UpdatesMetaDao,
        networkDataSource: NetworkDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): ContentItemRepositoryImpl {
        return ContentItemRepositoryImpl(
            contentDao = contentDao,
            contentTagsDao = contentTagsDao,
            updatesMetaDao = updatesMetaDao,
            networkDataSource = networkDataSource,
            ioDispatcher = ioDispatcher,
        )
    }

    @Singleton
    @Provides
    fun provideContentItemRepository(repository: ContentItemRepositoryImpl): ContentItemRepository = repository

    @Singleton
    @Provides
    fun provideContentPagingRepository(repository: ContentItemRepositoryImpl): ContentPagingRepository = repository

    @Singleton
    @Provides
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

    @Singleton
    @Provides
    fun provideUserProfileRepository(
        embeddingDao: ArticleEmbeddingDao,
        contentInteractionStatsDao: ContentInteractionStatsDao,
        userProfileDao: UserProfileDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(
            embeddingDao = embeddingDao,
            contentInteractionStatsDao = contentInteractionStatsDao,
            userProfileDao = userProfileDao,
            ioDispatcher = ioDispatcher,
        )
    }

    @Singleton
    @Provides
    fun provideAnalyticsService(
        eventLogDao: EventLogDao,
        userProfileRepository: UserProfileRepository,
        @ApplicationScope applicationScope: CoroutineScope,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): AnalyticsService {
        return AnalyticsServiceImpl(
            eventLogDao = eventLogDao,
            userProfileRepository = userProfileRepository,
            applicationScope = applicationScope,
            defaultDispatcher = defaultDispatcher,
            ioDispatcher = ioDispatcher,
        )
    }

    @Singleton
    @Provides
    fun provideRecommendationRepository(
        recommendationDao: RecommendationDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): RecommendationRepository {
        return RecommendationRepositoryImpl(
            recommendationDao = recommendationDao,
            ioDispatcher = ioDispatcher,
        )
    }
}
