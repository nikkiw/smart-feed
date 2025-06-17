package com.core.data.di

import com.core.data.repository.ContentItemRepositoryImpl
import com.core.data.repository.RecommendationRepositoryImpl
import com.core.data.repository.UserProfileRepositoryImpl
import com.core.data.service.AnalyticsServiceImpl
import com.core.data.service.RecommenderImpl
import com.core.database.content.ContentDao
import com.core.database.content.ContentTagsDao
import com.core.database.content.UpdatesMetaDao
import com.core.database.embeding.ArticleEmbeddingDao
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.event.EventLogDao
import com.core.database.recommendation.RecommendationDao
import com.core.database.userprofile.UserProfileDao
import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.RecommendationRepository
import com.core.domain.repository.UserProfileRepository
import com.core.domain.service.AnalyticsService
import com.core.domain.service.Recommender
import com.core.networks.datasource.NetworkDataSource
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
    fun provideContentItemRepository(
        contentDao: ContentDao,
        contentTagsDao: ContentTagsDao,
        updatesMetaDao: UpdatesMetaDao,
        networkDataSource: NetworkDataSource,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): ContentItemRepository {
        return ContentItemRepositoryImpl(
            contentDao = contentDao,
            contentTagsDao = contentTagsDao,
            updatesMetaDao = updatesMetaDao,
            networkDataSource = networkDataSource,
            ioDispatcher = ioDispatcher
        )
    }

    @Singleton
    @Provides
    fun provideRecommender(
        userProfileRepository: UserProfileRepository,
        contentInteractionStatsDao: ContentInteractionStatsDao,
        contentDao: ContentDao,
        articleEmbeddingDao: ArticleEmbeddingDao,
        recommendationDao: RecommendationDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        @ApplicationScope applicationScope: CoroutineScope
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
            lambda = 0.5f
        )
    }

    @Singleton
    @Provides
    fun provideUserProfileRepository(
        embeddingDao: ArticleEmbeddingDao,
        contentInteractionStatsDao: ContentInteractionStatsDao,
        userProfileDao: UserProfileDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): UserProfileRepository {
        return UserProfileRepositoryImpl(
            embeddingDao = embeddingDao,
            contentInteractionStatsDao = contentInteractionStatsDao,
            userProfileDao = userProfileDao,
            ioDispatcher = ioDispatcher
        )
    }


    @Singleton
    @Provides
    fun provideAnalyticsService(
        eventLogDao: EventLogDao,
        userProfileRepository: UserProfileRepository,
        @ApplicationScope applicationScope: CoroutineScope,
        @DefaultDispatcher defaultDispatcher: CoroutineDispatcher,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): AnalyticsService {
        return AnalyticsServiceImpl(
            eventLogDao = eventLogDao,
            userProfileRepository = userProfileRepository,
            applicationScope = applicationScope,
            defaultDispatcher = defaultDispatcher,
            ioDispatcher = ioDispatcher
        )
    }


    @Singleton
    @Provides
    fun provideRecommendationRepository(
        recommendationDao: RecommendationDao,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): RecommendationRepository {
        return RecommendationRepositoryImpl(
            recommendationDao = recommendationDao,
            ioDispatcher = ioDispatcher
        )
    }


}