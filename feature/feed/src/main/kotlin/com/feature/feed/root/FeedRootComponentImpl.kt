package com.feature.feed.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentId
import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.AnalyticsService
import com.core.domain.usecase.content.GetContentItemUseCase
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.recommendation.RecommendForArticleUseCase
import com.core.domain.usecase.recommendation.RecommendForUserUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import com.core.observers.ConnectivityRepository
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.article.ArticleItemComponentImpl
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.BottomBarComponentImpl
import com.feature.feed.bottombar.model.BottomBarState
import com.feature.feed.master.FeedMasterComponent
import com.feature.feed.master.FeedMasterComponentImpl
import com.feature.feed.recommendation.RecommendationListComponent
import com.feature.feed.recommendation.RecommendationListComponentImpl
import com.feature.feed.root.FeedRootComponent.Child.ArticleScreen
import com.feature.feed.root.FeedRootComponent.Child.FeedScreen
import com.feature.feed.root.FeedRootComponent.Child.RecommendationScreen
import com.feature.feed.root.FeedRootComponent.Config
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject


class FeedRootComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    private val contentItemRepository: ContentItemRepository,
    private val getContentUseCase: GetContentUseCase,
    private val syncContentUseCase: SyncContentUseCase,
    private val getContentItemUseCase: GetContentItemUseCase,
    private val analyticsService: AnalyticsService,
    private val recommendForUserUseCase: RecommendForUserUseCase,
    private val recommendForArticleUseCase: RecommendForArticleUseCase,
    private val connectivityRepository: ConnectivityRepository,
    ) : FeedRootComponent, ComponentContext by componentContext {

    @AssistedFactory
    interface FeedRootComponentFactory : FeedRootComponent.Factory {
        override fun invoke(componentContext: ComponentContext): FeedRootComponentImpl
    }


    internal val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, FeedRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(), // Or null to disable navigation state saving
            initialConfiguration = Config.FeedScreenConfig,
            handleBackButton = true, // Pop the back stack on back button press
            childFactory = ::createChild
        )

    override val bottomBar: BottomBarComponent = BottomBarComponentImpl(
        componentContext = childContext(key = "filterSort"),
        onTabBarChanged = {
            when (it) {
                BottomBarState.List -> {
                    navigation.replaceAll(Config.FeedScreenConfig)
                }

                BottomBarState.Recommendation -> {
                    navigation.replaceAll(Config.RecommendationScreenConfig)
                }
            }
        }
    )


    override fun pop(onComplete: (Boolean) -> Unit) {
        navigation.pop(onComplete)
    }

    internal fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): FeedRootComponent.Child =
        when (config) {
            is Config.FeedScreenConfig -> FeedScreen(
                itemList(
                    componentContext
                )
            )

            is Config.ArticleScreenConfig -> ArticleScreen(
                itemDetails(
                    componentContext,
                    config
                )
            )

            Config.RecommendationScreenConfig -> RecommendationScreen(
                recommendationList(
                    componentContext
                )
            )
        }

    @OptIn(DelicateDecomposeApi::class)
    private fun itemList(componentContext: ComponentContext): FeedMasterComponent =
        FeedMasterComponentImpl(
            componentContext = componentContext,
            contentItemRepository = contentItemRepository,
            getContentUseCase = getContentUseCase,
            syncContentUseCase = syncContentUseCase,
            connectivityRepository = connectivityRepository,
            onListItemClick = {
                navigation.pushToFront(Config.ArticleScreenConfig(itemId = it.value))
            }
        )

    @OptIn(DelicateDecomposeApi::class)
    private fun recommendationList(componentContext: ComponentContext): RecommendationListComponent =
        RecommendationListComponentImpl(
            componentContext = componentContext,
            recommendForUserUseCase = recommendForUserUseCase,
            connectivityRepository = connectivityRepository,
            onItemClick = {
                navigation.pushToFront(Config.ArticleScreenConfig(itemId = it.value))
            }
        )


    @OptIn(DelicateDecomposeApi::class)
    private fun itemDetails(
        componentContext: ComponentContext,
        config: Config.ArticleScreenConfig
    ): ArticleItemComponent =
        ArticleItemComponentImpl(
            componentContext = componentContext,
            getContentItemUseCase = getContentItemUseCase,
            recommendForArticleUseCase = recommendForArticleUseCase,
            analyticsService = analyticsService,
            itemId = ContentId(config.itemId),
            onFinished = {
                navigation.pop()
            },
            onClickItem = {
                navigation.pushToFront(Config.ArticleScreenConfig(itemId = it.value))
            }
        )


}
