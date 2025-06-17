package com.feature.feed.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.master.FeedMasterComponent
import com.feature.feed.recommendation.RecommendationListComponent
import kotlinx.serialization.Serializable


/**
 * Root component for article feed, responsible for setting up filter/sorting, displaying the feed and creating ArticleItemComponent.
 */
interface FeedRootComponent {

    val childStack: Value<ChildStack<*, Child>>

    val bottomBar: BottomBarComponent


    fun pop(onComplete: (Boolean) -> Unit)

    @Serializable
    sealed class Config {
        @Serializable
        data object FeedScreenConfig : Config()

        @Serializable
        data class ArticleScreenConfig(val itemId: String) : Config()

        @Serializable
        data object RecommendationScreenConfig : Config()
    }

    sealed class Child {
        data class FeedScreen(val component: FeedMasterComponent) : Child()
        data class ArticleScreen(val component: ArticleItemComponent) : Child()
        data class RecommendationScreen(val component: RecommendationListComponent) : Child()
    }

    fun interface Factory {
        operator fun invoke(componentContext: ComponentContext): FeedRootComponent
    }

}