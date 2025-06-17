package com.core.data.di

import com.core.data.usecase.content.GetContentItemUseCaseImpl
import com.core.data.usecase.content.GetContentUseCaseImpl
import com.core.data.usecase.recommendation.RecommendForArticleUseCaseImpl
import com.core.data.usecase.recommendation.RecommendForUserUseCaseImpl
import com.core.data.usecase.sync.ContentFetchScheduleUseCaseImpl
import com.core.data.usecase.sync.SyncContentUseCaseImpl
import com.core.domain.usecase.content.GetContentItemUseCase
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.recommendation.RecommendForArticleUseCase
import com.core.domain.usecase.recommendation.RecommendForUserUseCase
import com.core.domain.usecase.sync.ContentFetchScheduleUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Singleton
    @Binds
    abstract fun bindSyncContentUseCase(
        syncContentUseCaseImpl: SyncContentUseCaseImpl
    ): SyncContentUseCase


    @Singleton
    @Binds
    abstract fun bindContentFetchScheduleUseCase(
        contentFetchScheduleUseCaseImpl: ContentFetchScheduleUseCaseImpl
    ): ContentFetchScheduleUseCase


    @Singleton
    @Binds
    abstract fun bindGetContentItemUseCase(
        getContentItemUseCaseImpl: GetContentItemUseCaseImpl
    ): GetContentItemUseCase


    @Singleton
    @Binds
    abstract fun bindGetContentUseCase(
        getContentUseCaseImpl: GetContentUseCaseImpl
    ): GetContentUseCase

    @Singleton
    @Binds
    abstract fun bindRecommendForArticleUseCase(
        recommendForArticleUseCaseImpl: RecommendForArticleUseCaseImpl
    ): RecommendForArticleUseCase

    @Singleton
    @Binds
    abstract fun bindRecommendForUserUseCase(
        recommendForUserUseCaseImpl: RecommendForUserUseCaseImpl
    ): RecommendForUserUseCase
}