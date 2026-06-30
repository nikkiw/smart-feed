package com.ndev.android.smart.feed.ui

import android.widget.FrameLayout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.DefaultViewContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.feature.feed.root.FeedRootComponent
import com.feature.feed.root.FeedRootComponentView
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class FeedRootViewHost
    @Inject
    constructor() {
        @OptIn(ExperimentalDecomposeApi::class)
        fun attach(
            container: FrameLayout,
            component: FeedRootComponent,
            lifecycle: Lifecycle,
        ) {
            val viewContext =
                DefaultViewContext(
                    parent = container,
                    lifecycle = lifecycle,
                )
            val rootView = viewContext.FeedRootComponentView(component)
            container.removeAllViews()
            container.addView(rootView)
        }
    }
