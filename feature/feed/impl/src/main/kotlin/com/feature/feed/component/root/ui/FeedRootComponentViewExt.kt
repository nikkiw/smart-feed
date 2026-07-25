package com.feature.feed.component.root.ui

import android.view.View
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.child
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.arkivanov.decompose.extensions.android.stack.StackRouterView
import com.core.di.ImageLoaderEntryPoint
import com.feature.feed.R
import com.feature.feed.component.article.ArticleItemView
import com.feature.feed.component.bottombar.BottomBarView
import com.feature.feed.component.list.ui.ArticleCardRenderMode
import com.feature.feed.component.master.FeedMasterView
import com.feature.feed.component.recommendation.RecommendationListView
import com.feature.feed.root.FeedRootComponent
import dagger.hilt.android.EntryPointAccessors
import io.noties.markwon.Markwon

@OptIn(ExperimentalDecomposeApi::class)
@Suppress("FunctionName")
fun ViewContext.FeedRootComponentView(
    component: FeedRootComponent,
    articleCardRenderMode: ArticleCardRenderMode,
): View {
    val layout = layoutInflater.inflate(R.layout.feed_root, parent, false)
    val routerView: StackRouterView = layout.findViewById(R.id.router)

    val imageLoader =
        EntryPointAccessors.fromApplication<ImageLoaderEntryPoint>(
            parent.context.applicationContext,
        ).imageLoader()

    // Контейнеры во View
    child(layout.findViewById(R.id.bottom_menu_view)) {
        BottomBarView(component.bottomBar)
    }

    val markwon =
        Markwon.builder(parent.context.applicationContext)
//            .usePlugin(HtmlPlugin.create())
            .build()
    routerView.children(
        stack = component.childStack,
        lifecycle = lifecycle,
        replaceChildView =
            viewSwitcherWithRegistry { child ->
                when (child) {
                    is FeedRootComponent.Child.ArticleScreen ->
                        ArticleItemView(
                            child.component,
                            imageLoader,
                            markwon,
                        )

                    is FeedRootComponent.Child.FeedScreen ->
                        FeedMasterView(child.component, articleCardRenderMode)
                    is FeedRootComponent.Child.RecommendationScreen ->
                        RecommendationListView(
                            child.component,
                            articleCardRenderMode,
                        )
                }
            },
    )
    return layout
}
