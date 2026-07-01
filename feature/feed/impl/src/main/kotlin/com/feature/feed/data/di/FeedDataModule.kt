package com.feature.feed.data.di

import com.core.di.IoDispatcher
import com.core.networks.datasource.NetworkDataSource
import com.core.paging.ContentPagingRepository
import com.feature.feed.data.repository.ContentItemRepositoryImpl
import com.feature.feed.data.usecase.content.GetContentItemUseCaseImpl
import com.feature.feed.data.usecase.sync.ContentFetchScheduleUseCaseImpl
import com.feature.feed.data.usecase.sync.SyncContentUseCaseImpl
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.GetContentItemUseCase
import com.feature.feed.domain.usecase.sync.ContentFetchScheduleUseCase
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.local.content.ContentDao
import com.feature.feed.local.content.ContentTagsDao
import com.feature.feed.local.content.UpdatesMetaDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedDataModule {
    @Binds
    @Singleton
    abstract fun bindContentItemRepository(repository: ContentItemRepositoryImpl): ContentItemRepository

    @Binds
    @Singleton
    abstract fun bindContentPagingRepository(repository: ContentItemRepositoryImpl): ContentPagingRepository

    @Binds
    @Singleton
    abstract fun bindSyncContentUseCase(syncContentUseCaseImpl: SyncContentUseCaseImpl): SyncContentUseCase

    @Binds
    @Singleton
    abstract fun bindContentFetchScheduleUseCase(
        contentFetchScheduleUseCaseImpl: ContentFetchScheduleUseCaseImpl,
    ): ContentFetchScheduleUseCase

    @Binds
    @Singleton
    abstract fun bindGetContentItemUseCase(getContentItemUseCaseImpl: GetContentItemUseCaseImpl): GetContentItemUseCase

    companion object {
        @Provides
        @Singleton
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
    }
}
