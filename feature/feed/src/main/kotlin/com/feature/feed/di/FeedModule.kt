package com.feature.feed.di

import com.feature.feed.root.FeedRootComponent
import com.feature.feed.root.FeedRootComponentImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
abstract class FeedModule {
    @Binds
    abstract fun bindFeedRootFactory(
        factory: FeedRootComponentImpl.FeedRootComponentFactory,
    ): FeedRootComponent.Factory
}
