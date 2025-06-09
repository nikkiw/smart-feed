package com.feature.feed.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import com.core.domain.model.ContentItemId
import com.core.domain.repository.ContentItemRepository
import com.core.domain.usecase.content.GetContentItemUseCase
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.article.ArticleItemComponentImpl
import com.feature.feed.master.FeedMasterComponent
import com.feature.feed.master.FeedMasterComponentImpl
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
) : FeedRootComponent, ComponentContext by componentContext {

    @AssistedFactory
    interface FeedRootComponentFactory : FeedRootComponent.Factory {
        override fun invoke(componentContext: ComponentContext): FeedRootComponentImpl
    }


    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, FeedRootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(), // Or null to disable navigation state saving
            initialConfiguration = Config.FeedScreenConfig,
            handleBackButton = true, // Pop the back stack on back button press
            childFactory = ::createChild
        )

    override fun pop(onComplete: (Boolean) -> Unit) {
       navigation.pop(onComplete)
    }

    private fun createChild(
        config: Config,
        componentContext: ComponentContext
    ): FeedRootComponent.Child =
        when (config) {
            is Config.FeedScreenConfig -> FeedRootComponent.Child.FeedScreen(
                itemList(
                    componentContext
                )
            )

            is Config.ArticleScreenConfig -> FeedRootComponent.Child.ArticleScreen(
                itemDetails(
                    componentContext,
                    config
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
            onListItemClick = {
                navigation.push(Config.ArticleScreenConfig(itemId = it.value))
            }
        )

    private fun itemDetails(
        componentContext: ComponentContext,
        config: Config.ArticleScreenConfig
    ): ArticleItemComponent =
        ArticleItemComponentImpl(
            componentContext = componentContext,
            getContentItemUseCase = getContentItemUseCase,
            itemId = ContentItemId(config.itemId),
            onFinished = {
                navigation.pop()
            }
        )
}
