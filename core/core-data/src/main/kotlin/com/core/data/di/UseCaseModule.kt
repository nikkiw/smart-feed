package com.core.data.di

import com.core.paging.GetPagedContentUseCase
import com.core.paging.GetPagedContentUseCaseImpl
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
    abstract fun bindGetPagedContentUseCase(
        getPagedContentUseCaseImpl: GetPagedContentUseCaseImpl,
    ): GetPagedContentUseCase
}
