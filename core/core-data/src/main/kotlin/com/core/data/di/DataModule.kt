package com.core.data.di

import com.core.database.content.ContentDao
import com.core.database.content.UpdatesMetaDao
import com.core.di.IoDispatcher
import com.core.domain.repository.ContentItemRepository
import com.core.data.repository.ContentItemRepositoryImpl
import com.core.database.content.ContentTagsDao
import com.core.networks.datasource.NetworkDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
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
}