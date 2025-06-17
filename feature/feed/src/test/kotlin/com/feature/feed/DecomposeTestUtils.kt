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
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItemPreview
import com.core.domain.model.ShortDescription
import com.core.domain.model.Title
import com.core.domain.repository.ContentItemRepository
import com.core.domain.service.AnalyticsService
import com.core.domain.usecase.content.GetContentItemUseCase
import com.core.domain.usecase.content.GetContentUseCase
import com.core.domain.usecase.recommendation.RecommendForArticleUseCase
import com.core.domain.usecase.recommendation.RecommendForUserUseCase
import com.core.domain.usecase.sync.SyncContentUseCase
import com.core.observers.ConnectivityRepository
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.BottomBarComponentImpl
import com.feature.feed.bottombar.model.BottomBarState
import com.feature.feed.root.FeedRootComponent
import com.feature.feed.root.FeedRootComponentImpl
import com.google.common.truth.FailureMetadata
import com.google.common.truth.Subject
import com.google.common.truth.Truth
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk


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
            componentContext = DefaultComponentContext(
                lifecycle = lifecycle,
                stateKeeper = stateKeeper,
                instanceKeeper = instanceKeeper
            ),
            lifecycle = lifecycle
        )
    }

    class TestComponentContext(
        val componentContext: ComponentContext,
        val lifecycle: LifecycleRegistry
    ) {
        fun startLifecycle() {
            lifecycle.create()
            lifecycle.resume()
        }

        fun destroyLifecycle() {
            lifecycle.destroy()
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
        private val actual: ChildStack<*, FeedRootComponent.Child>
    ) : Subject(metadata, actual) {

        companion object {
            /** Truth factory for ChildStackSubject */
            private val FACTORY =
                object : Factory<ChildStackSubject, ChildStack<*, FeedRootComponent.Child>> {
                    override fun createSubject(
                        metadata: FailureMetadata,
                        actual: ChildStack<*, FeedRootComponent.Child>?
                    ): ChildStackSubject {
                        return ChildStackSubject(metadata, actual!!)
                    }
                }

            /** Entry point to use in tests: */
            @JvmStatic
            fun assertThat(
                childStack: ChildStack<*, FeedRootComponent.Child>
            ): ChildStackSubject {
                return Truth.assertAbout(FACTORY).that(childStack)
            }
        }

        fun hasActiveConfiguration(expectedConfig: FeedRootComponent.Config): ChildStackSubject {
            check("active.configuration").that(actual.active.configuration)
                .isEqualTo(expectedConfig)
            return this
        }

        fun hasActiveChildOfType(
            expectedType: Class<out FeedRootComponent.Child>
        ): ChildStackSubject {
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

        fun hasBackStackContaining(
            expectedConfig: FeedRootComponent.Config
        ): ChildStackSubject {
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
        return MockDependencies(
            contentItemRepository = mockk(relaxed = true),
            getContentUseCase = mockk(relaxed = true),
            syncContentUseCase = mockk(relaxed = true),
            getContentItemUseCase = mockk(relaxed = true),
            analyticsService = mockk(relaxed = true),
            recommendForUserUseCase = mockk(relaxed = true),
            recommendForArticleUseCase = mockk(relaxed = true),
            connectivityRepository = mockk(relaxed = true)
        )
    }

    data class MockDependencies(
        val contentItemRepository: ContentItemRepository,
        val getContentUseCase: GetContentUseCase,
        val syncContentUseCase: SyncContentUseCase,
        val getContentItemUseCase: GetContentItemUseCase,
        val analyticsService: AnalyticsService,
        val recommendForUserUseCase: RecommendForUserUseCase,
        val recommendForArticleUseCase: RecommendForArticleUseCase,
        val connectivityRepository: ConnectivityRepository
    )

    fun createFeedRootComponent(
        componentContext: ComponentContext = DecomposeTestUtils.createTestComponentContext().componentContext,
        dependencies: MockDependencies = createMockDependencies()
    ): FeedRootComponentImpl {
        return FeedRootComponentImpl(
            componentContext = componentContext,
            contentItemRepository = dependencies.contentItemRepository,
            getContentUseCase = dependencies.getContentUseCase,
            syncContentUseCase = dependencies.syncContentUseCase,
            getContentItemUseCase = dependencies.getContentItemUseCase,
            analyticsService = dependencies.analyticsService,
            recommendForUserUseCase = dependencies.recommendForUserUseCase,
            recommendForArticleUseCase = dependencies.recommendForArticleUseCase,
            connectivityRepository = dependencies.connectivityRepository
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

    fun verifyNavigationToArticle(
        component: FeedRootComponent,
        itemId: String
    ) {
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
        description: String = "Test Description"
    ): ContentItemPreview {
        return mockk<ContentItemPreview.ArticlePreview>(relaxed = true) {
            every { this@mockk.id } returns ContentId(id)
            every { this@mockk.title } returns Title(title)
            every { this@mockk.short } returns ShortDescription(description)
        }
    }

    fun createPagingData(items: List<ContentItemPreview>): PagingData<ContentItemPreview> {
        return mockk<PagingData<ContentItemPreview>>(relaxed = true)
    }
}

/**
 * Assertion helpers
 */
fun assertBottomBarState(
    bottomBar: BottomBarComponent,
    expectedState: BottomBarState
) {
    when (bottomBar) {
        is BottomBarComponentImpl -> {
            assertThat(bottomBar.state).isEqualTo(expectedState)
        }

        else -> throw AssertionError("BottomBar is not of expected type")
    }
}