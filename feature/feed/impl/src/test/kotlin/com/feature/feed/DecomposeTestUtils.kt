package com.feature.feed

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.essenty.instancekeeper.InstanceKeeperDispatcher
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.statekeeper.StateKeeperDispatcher
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.core.analytics.api.AnalyticsService
import com.core.content.model.ContentId
import com.core.content.model.ShortDescription
import com.core.content.model.Title
import com.core.observers.ConnectivityRepository
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.model.BottomBarState
import com.feature.feed.component.bottombar.BottomBarComponentImpl
import com.feature.feed.component.list.DefaultFeedListComponentFactory
import com.feature.feed.component.root.FeedRootComponentImpl
import com.feature.feed.data.usecase.content.GetPagedContentUseCase
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.GetContentItemUseCase
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.root.FeedRootComponent
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import com.feature.recommendation.domain.usecase.RecommendForUserUseCase
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

/**
 * Test utilities for Decompose components testing
 */
object DecomposeTestUtils {
    /**
     * Creates a test ComponentContext with proper lifecycle management
     */
    fun createTestComponentContext(): TestComponentContext {
        val lifecycle = LifecycleRegistry()
        val stateKeeper = StateKeeperDispatcher()
        val instanceKeeper = InstanceKeeperDispatcher()

        return TestComponentContext(
            componentContext =
            DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                instanceKeeper = instanceKeeper,
            ),
            lifecycle = lifecycle,
            instanceKeeper = instanceKeeper,
        )
    }

    class TestComponentContext(
        val componentContext: ComponentContext,
        val lifecycle: LifecycleRegistry,
        private val instanceKeeper: InstanceKeeperDispatcher,
    ) {
        fun startLifecycle() {
            lifecycle.create()
            lifecycle.resume()
        }

        fun destroyLifecycle() {
            lifecycle.destroy()
        }

        fun recreate(): TestComponentContext {
            val recreatedLifecycle = LifecycleRegistry()
            return TestComponentContext(
                componentContext =
                DefaultComponentContext(
                    lifecycle = recreatedLifecycle,
                    stateKeeper = StateKeeperDispatcher(),
                    instanceKeeper = instanceKeeper,
                ),
                lifecycle = recreatedLifecycle,
                instanceKeeper = instanceKeeper,
            )
        }
    }
}

/**
 * Custom Truth subjects for better assertions
 */
object FeedComponentSubjects {
    fun assertThat(childStack: ChildStack<*, FeedRootComponent.Child>): ChildStackSubject {
        return ChildStackSubject.assertThat(childStack)
    }

    class ChildStackSubject(
        metadata: FailureMetadata,
        private val actual: ChildStack<*, FeedRootComponent.Child>,
    ) : Subject(metadata, actual) {
        companion object {
            /** Truth factory for ChildStackSubject */
            private val FACTORY =
                Factory<ChildStackSubject, ChildStack<*, FeedRootComponent.Child>> { metadata, actual ->
                    ChildStackSubject(
                        metadata,
                        actual!!,
                    )
                }

            /** Entry point to use in tests: */
            @JvmStatic
            fun assertThat(childStack: ChildStack<*, FeedRootComponent.Child>): ChildStackSubject {
                return Truth.assertAbout(FACTORY).that(childStack)
            }
        }

        fun hasActiveConfiguration(expectedConfig: FeedRootComponent.Config): ChildStackSubject {
            check("active.configuration").that(actual.active.configuration)
                .isEqualTo(expectedConfig)
            return this
        }

        fun hasActiveChildOfType(expectedType: Class<out FeedRootComponent.Child>): ChildStackSubject {
            check("active.instance").that(actual.active.instance)
                .isInstanceOf(expectedType)
            return this
        }

        fun hasBackStackSize(expectedSize: Int): ChildStackSubject {
            check("backStack").that(actual.backStack)
                .hasSize(expectedSize)
            return this
        }

        fun hasEmptyBackStack(): ChildStackSubject {
            check("backStack").that(actual.backStack)
                .isEmpty()
            return this
        }

        fun hasBackStackContaining(expectedConfig: FeedRootComponent.Config): ChildStackSubject {
            val configs = actual.backStack.map { it.configuration }
            check("backStack.configurationList").that(configs)
                .contains(expectedConfig)
            return this
        }
    }
}

/**
 * Test data builders for creating test objects
 */
object FeedTestDataBuilder {
    fun createMockDependencies(): MockDependencies {
        val contentItemRepository = mockk<ContentItemRepository>(relaxed = true)
        every { contentItemRepository.observeHasContent() } returns emptyFlow()
        val connectivityRepository = mockk<ConnectivityRepository>(relaxed = true)
        every { connectivityRepository.isConnected } returns MutableStateFlow(true)
        val recommendForUserUseCase = mockk<RecommendForUserUseCase>(relaxed = true)
        every { recommendForUserUseCase.invoke() } returns emptyFlow()
        return MockDependencies(
            contentItemRepository = contentItemRepository,
            getPagedContentUseCase = mockk(relaxed = true),
            syncContentUseCase = mockk(relaxed = true),
            getContentItemUseCase = mockk(relaxed = true),
            analyticsService = mockk(relaxed = true),
            recommendForUserUseCase = recommendForUserUseCase,
            recommendForArticleUseCase = mockk(relaxed = true),
            connectivityRepository = connectivityRepository,
        )
    }

    data class MockDependencies(
        val contentItemRepository: ContentItemRepository,
        val getPagedContentUseCase: GetPagedContentUseCase,
        val syncContentUseCase: SyncContentUseCase,
        val getContentItemUseCase: GetContentItemUseCase,
        val analyticsService: AnalyticsService,
        val recommendForUserUseCase: RecommendForUserUseCase,
        val recommendForArticleUseCase: RecommendForArticleUseCase,
        val connectivityRepository: ConnectivityRepository,
    )

    fun createFeedRootComponent(
        componentContext: ComponentContext = DecomposeTestUtils.createTestComponentContext().componentContext,
        dependencies: MockDependencies = createMockDependencies(),
    ): FeedRootComponentImpl {
        return FeedRootComponentImpl(
            componentContext = componentContext,
            contentItemRepository = dependencies.contentItemRepository,
            feedListComponentFactory =
            DefaultFeedListComponentFactory(
                storeFactory = DefaultStoreFactory(),
                getPagedContentUseCase = dependencies.getPagedContentUseCase,
                syncContentUseCase = dependencies.syncContentUseCase,
                connectivityRepository = dependencies.connectivityRepository,
                contentItemRepository = dependencies.contentItemRepository,
            ),
            getContentItemUseCase = dependencies.getContentItemUseCase,
            analyticsService = dependencies.analyticsService,
            recommendForUserUseCase = dependencies.recommendForUserUseCase,
            recommendForArticleUseCase = dependencies.recommendForArticleUseCase,
            connectivityRepository = dependencies.connectivityRepository,
            syncContentUseCase = dependencies.syncContentUseCase,
            storeFactory = DefaultStoreFactory(),
        )
    }
}

/**
 * Test scenarios for navigation testing
 */
object NavigationTestScenarios {
    fun verifyInitialState(component: FeedRootComponent) {
        FeedComponentSubjects.assertThat(component.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.FeedScreenConfig)
            .hasActiveChildOfType(FeedRootComponent.Child.FeedScreen::class.java)
            .hasEmptyBackStack()
    }

    fun verifyNavigationToArticle(component: FeedRootComponent, itemId: String) {
        FeedComponentSubjects.assertThat(component.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.ArticleScreenConfig(itemId))
            .hasActiveChildOfType(FeedRootComponent.Child.ArticleScreen::class.java)
    }

    fun verifyNavigationToRecommendations(component: FeedRootComponent) {
        FeedComponentSubjects.assertThat(component.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.RecommendationScreenConfig)
            .hasActiveChildOfType(FeedRootComponent.Child.RecommendationScreen::class.java)
    }
}

/**
 * Extension functions for easier testing
 */
fun FeedRootComponent.getCurrentConfig(): FeedRootComponent.Config {
    return this.childStack.value.active.configuration as FeedRootComponent.Config
}

fun FeedRootComponent.getCurrentChild(): FeedRootComponent.Child {
    return this.childStack.value.active.instance
}

fun FeedRootComponent.getBackStackSize(): Int {
    return this.childStack.value.backStack.size
}

/**
 * Mock factories for creating test doubles
 */
object MockFactories {
    fun createContentItemPreview(
        id: String = "test-id",
        title: String = "Test Title",
        description: String = "Test Description",
    ): ContentItemPreview {
        return mockk<ContentItemPreview.ArticlePreview>(relaxed = true) {
            every { this@mockk.id } returns ContentId(id)
            every { this@mockk.title } returns Title(title)
            every { this@mockk.short } returns ShortDescription(description)
        }
    }

    @Suppress("UnusedParameter")
    fun createPagingData(items: List<ContentItemPreview>): PagingData<ContentItemPreview> {
        return mockk<PagingData<ContentItemPreview>>(relaxed = true)
    }
}

/**
 * Assertion helpers
 */
fun assertBottomBarState(bottomBar: BottomBarComponent, expectedState: BottomBarState) {
    when (bottomBar) {
        is BottomBarComponentImpl -> {
            assertThat(bottomBar.state).isEqualTo(expectedState)
        }

        else -> throw AssertionError("BottomBar is not of expected type")
    }
}
