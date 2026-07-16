package com.feature.feed.di

import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.feature.feed.component.list.DefaultFeedListComponentFactory
import com.feature.feed.component.root.FeedRootComponentImpl
import com.feature.feed.list.FeedListComponent
import com.feature.feed.root.FeedRootComponent
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class FeedModule {
    companion object {
        @Provides
        fun provideStoreFactory(): StoreFactory = DefaultStoreFactory()
    }

    @Binds
    abstract fun bindFeedRootFactory(
        factory: FeedRootComponentImpl.FeedRootComponentFactory,
    ): FeedRootComponent.Factory

    @Binds
    abstract fun bindFeedListComponentFactory(factory: DefaultFeedListComponentFactory): FeedListComponent.Factory
}
