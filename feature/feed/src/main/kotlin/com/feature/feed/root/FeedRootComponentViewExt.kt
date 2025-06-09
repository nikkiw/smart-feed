package com.feature.feed.root

import android.view.View
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.arkivanov.decompose.extensions.android.stack.StackRouterView
import com.core.di.ImageLoaderEntryPoint
import com.feature.feed.R
import com.feature.feed.article.ArticleItemView
import com.feature.feed.master.FeedMasterView
import dagger.hilt.android.EntryPointAccessors


@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.FeedRootComponentView(component: FeedRootComponent): View {
    val layout = layoutInflater.inflate(R.layout.feed_root, parent, false)
    val routerView: StackRouterView = layout.findViewById(R.id.router)

    val imageLoader = EntryPointAccessors.fromApplication<ImageLoaderEntryPoint>(
        parent.context.applicationContext
    ).imageLoader()

    routerView.children(
        stack = component.childStack,
        lifecycle = lifecycle,
        replaceChildView = viewSwitcher { child ->
            when (child) {
                is FeedRootComponent.Child.ArticleScreen -> ArticleItemView(
                    child.component,
                    imageLoader
                )

                is FeedRootComponent.Child.FeedScreen -> FeedMasterView(child.component)
            }
        },
    )
    return layout
}