package com.core.data.di

import com.core.data.repository.UserProfileRepositoryImpl
import com.core.data.service.AnalyticsServiceImpl
import com.core.database.event.EventLogDao
import com.core.database.userprofile.UserProfileDao
import com.core.di.ApplicationScope
import com.core.di.DefaultDispatcher
import com.core.di.IoDispatcher
import com.core.domain.repository.UserProfileRepository
import com.core.domain.service.AnalyticsService
import com.feature.recommendation.local.embedding.ArticleEmbeddingDao
import com.feature.recommendation.local.event.ContentInteractionStatsDao
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
}
