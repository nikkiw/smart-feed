package com.core.database.di

import com.core.database.AppDatabase
import com.core.database.event.EventLogDao
import com.core.database.userprofile.UserProfileDao
import com.feature.feed.local.content.ContentDao
import com.feature.feed.local.content.ContentTagsDao
import com.feature.feed.local.content.UpdatesMetaDao
import com.feature.recommendation.local.embedding.ArticleEmbeddingDao
import com.feature.recommendation.local.event.ContentInteractionStatsDao
import com.feature.recommendation.local.recommendation.RecommendationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DaoModule {
    @Singleton
    @Provides
    fun provideContentDao(db: AppDatabase): ContentDao {
        return db.contentDao()
    }

    @Singleton
    @Provides
    fun provideUpdatesMetaDao(db: AppDatabase): UpdatesMetaDao {
        return db.updatesMetaDao()
    }

    @Singleton
    @Provides
    fun provideContentTagsDao(db: AppDatabase): ContentTagsDao {
        return db.contentTagsDao()
    }

    @Singleton
    @Provides
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao {
        return db.userProfileDao()
    }

    @Singleton
    @Provides
    fun provideArticleEmbeddingDao(db: AppDatabase): ArticleEmbeddingDao {
        return db.articleEmbeddingDao()
    }

    @Singleton
    @Provides
    fun provideArticleInteractionStatsDao(db: AppDatabase): ContentInteractionStatsDao {
        return db.articleInteractionStatsDao()
    }

    @Singleton
    @Provides
    fun provideEventLogDao(db: AppDatabase): EventLogDao {
        return db.eventLogDao()
    }

    @Singleton
    @Provides
    fun provideRecommendationDao(db: AppDatabase): RecommendationDao {
        return db.recommendationDao()
    }
}
