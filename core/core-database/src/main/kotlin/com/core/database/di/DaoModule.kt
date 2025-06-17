package com.core.database.di

import com.core.database.AppDatabase
import com.core.database.content.ContentDao
import com.core.database.content.ContentTagsDao
import com.core.database.content.UpdatesMetaDao
import com.core.database.embeding.ArticleEmbeddingDao
import com.core.database.event.ContentInteractionStatsDao
import com.core.database.event.EventLogDao
import com.core.database.recommendation.RecommendationDao
import com.core.database.userprofile.UserProfileDao
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
    fun provideContentDao(
        db: AppDatabase
    ): ContentDao {
        return db.contentDao()
    }

    @Singleton
    @Provides
    fun provideUpdatesMetaDao(
        db: AppDatabase
    ): UpdatesMetaDao {
        return db.updatesMetaDao()
    }

    @Singleton
    @Provides
    fun provideContentTagsDao(
        db: AppDatabase
    ): ContentTagsDao {
        return db.contentTagsDao()
    }

    @Singleton
    @Provides
    fun provideUserProfileDao(
        db: AppDatabase
    ): UserProfileDao {
        return db.userProfileDao()
    }

    @Singleton
    @Provides
    fun provideArticleEmbeddingDao(
        db: AppDatabase
    ): ArticleEmbeddingDao {
        return db.articleEmbeddingDao()
    }

    @Singleton
    @Provides
    fun provideArticleInteractionStatsDao(
        db: AppDatabase
    ): ContentInteractionStatsDao {
        return db.articleInteractionStatsDao()
    }

    @Singleton
    @Provides
    fun provideEventLogDao(
        db: AppDatabase
    ): EventLogDao {
        return db.eventLogDao()
    }

    @Singleton
    @Provides
    fun provideRecommendationDao(
        db: AppDatabase
    ): RecommendationDao {
        return db.recommendationDao()
    }
}