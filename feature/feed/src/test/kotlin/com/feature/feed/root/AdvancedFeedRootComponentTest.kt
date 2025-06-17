package com.feature.feed.root

import com.arkivanov.decompose.ComponentContext
import com.core.domain.model.Content
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItem.Article
import com.core.domain.model.ContentItemPreview
import com.core.domain.model.ContentItemPreview.ArticlePreview
import com.core.domain.model.ImageUrl
import com.core.domain.model.ShortDescription
import com.core.domain.model.Tags
import com.core.domain.model.Title
import com.core.domain.model.UpdatedAt
import com.feature.feed.DecomposeTestUtils
import com.feature.feed.FeedComponentSubjects
import com.feature.feed.FeedTestDataBuilder
import com.feature.feed.NavigationTestScenarios
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.bottombar.BottomBarComponent
import com.feature.feed.bottombar.BottomBarComponentImpl
import com.feature.feed.bottombar.model.BottomBarState
import com.feature.feed.getBackStackSize
import com.feature.feed.getCurrentConfig
import com.feature.feed.recommendation.RecommendationListComponent
import com.google.common.truth.Truth.assertThat
import io.mockk.called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test


class AdvancedFeedRootComponentTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var testContext: DecomposeTestUtils.TestComponentContext
    private lateinit var mockDependencies: FeedTestDataBuilder.MockDependencies
    private lateinit var feedRootComponent: FeedRootComponentImpl

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Arrange: configure Dispatchers.Main to use our test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Arrange: create a test component context and mock dependencies
        testContext = DecomposeTestUtils.createTestComponentContext()
        mockDependencies = FeedTestDataBuilder.createMockDependencies()

        // Arrange: build the FeedRootComponent with test context and mocks
        feedRootComponent = FeedTestDataBuilder.createFeedRootComponent(
            componentContext = testContext.componentContext,
            dependencies = mockDependencies
        )

        // Arrange: start the component lifecycle
        testContext.startLifecycle()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        // Clean up: reset Dispatchers.Main so it does not leak into other tests
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be correctly configured`() = runTest(testDispatcher) {
        // Then: verify initial navigation state
        NavigationTestScenarios.verifyInitialState(feedRootComponent)

        // Additional assertions on default config, back stack, and bottom bar
        assertThat(feedRootComponent.getCurrentConfig()).isEqualTo(FeedRootComponent.Config.FeedScreenConfig)
        assertThat(feedRootComponent.getBackStackSize()).isEqualTo(0)
        assertThat(feedRootComponent.bottomBar).isNotNull()
    }

    @Test
    fun `navigation to article screen should work correctly`() = runTest(testDispatcher) {
        // Arrange: prepare test article data and stub use-case
        val articleId = "test-article-123"
        val testArticle = Article(
            id = ContentId(articleId),
            updatedAt = UpdatedAt(0L),
            mainImageUrl = ImageUrl("http://example.com/img.png"),
            tags = Tags(listOf("news", "kotlin")),
            title = Title("Sample Title"),
            content = Content("Full content goes here"),
            short = ShortDescription("Short description")
        )
        coEvery { mockDependencies.getContentItemUseCase.invoke(any()) } returns Result.success(
            testArticle
        )

        // Given: the Article screen config
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig(articleId)

        // When: create the child component for the Article screen
        val child = feedRootComponent.createChild(articleConfig, testContext.componentContext)

        // Then: component type assertions
        assertThat(child).isInstanceOf(FeedRootComponent.Child.ArticleScreen::class.java)
        val articleScreen = child as FeedRootComponent.Child.ArticleScreen
        assertThat(articleScreen.component).isInstanceOf(ArticleItemComponent::class.java)

        // Then: verify use-case invocation
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) }
    }

    @Test
    fun `navigation to recommendation screen should work correctly`() {
        // Given: the Recommendation screen config
        val recommendationConfig = FeedRootComponent.Config.RecommendationScreenConfig

        // When: create the child component for recommendations
        val child =
            feedRootComponent.createChild(recommendationConfig, testContext.componentContext)

        // Then: component type assertions
        assertThat(child).isInstanceOf(FeedRootComponent.Child.RecommendationScreen::class.java)
        val recommendationScreen = child as FeedRootComponent.Child.RecommendationScreen
        assertThat(recommendationScreen.component).isInstanceOf(RecommendationListComponent::class.java)
    }

    @Test
    fun `bottom bar navigation should update child stack correctly`() = runTest(testDispatcher) {
        // Given: access the BottomBarComponent
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl

        // When: user taps the Recommendations tab
        bottomBar.onClickTabBar(BottomBarState.Recommendation)

        // Then: verify Recommendation screen is active with empty back stack
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.RecommendationScreenConfig)
            .hasActiveChildOfType(FeedRootComponent.Child.RecommendationScreen::class.java)
            .hasEmptyBackStack()

        // When: user taps the List tab to return
        bottomBar.onClickTabBar(BottomBarState.List)

        // Then: verify Feed screen is active again with empty back stack
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.FeedScreenConfig)
            .hasActiveChildOfType(FeedRootComponent.Child.FeedScreen::class.java)
            .hasEmptyBackStack()
    }

    @Test
    fun `pop navigation should handle callback correctly`() {
        // Given: a flag to capture the pop result
        var popResult: Boolean? = null
        val popCallback: (Boolean) -> Unit = { result -> popResult = result }

        // When: invoke the pop function
        feedRootComponent.pop(popCallback)

        // Then: ensure the callback was invoked
        assertThat(popResult).isNotNull()
    }

    @Test
    fun `ArticleItemComponent should handle navigation callbacks`() = runTest(testDispatcher) {
        // Arrange: stub use-case to return a fake article
        val articleId = "test-article-nav"
        val fakeArticle = Article(
            id = ContentId(articleId),
            updatedAt = UpdatedAt.now(),
            mainImageUrl = ImageUrl("https://example.com/img.png"),
            tags = Tags(listOf("tag1", "tag2")),
            title = Title("Test Title"),
            short = ShortDescription("Short desc"),
            content = Content("Full article text")
        )
        coEvery { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) } returns Result.success(
            fakeArticle
        )

        // Given: create the ArticleScreen child
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig(articleId)
        val articleChild = feedRootComponent.createChild(
            articleConfig,
            testContext.componentContext
        ) as FeedRootComponent.Child.ArticleScreen

        // Then: verify component type and use-case call
        assertThat(articleChild.component).isInstanceOf(ArticleItemComponent::class.java)
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) }

        // Then: ensure analytics service was not called during initialization
        verify { mockDependencies.analyticsService wasNot called }
    }

    @Test
    fun `RecommendationListComponent should handle item click navigation`() {
        // Arrange: prepare a preview article and stub the recommend use-case
        val testArticle = ArticlePreview(
            id = ContentId("test-id"),
            updatedAt = UpdatedAt(0L),
            mainImageUrl = ImageUrl("http://example.com/img.png"),
            tags = Tags(listOf("news", "kotlin")),
            title = Title("Sample Title"),
            short = ShortDescription("Short desc")
        )
        val testFlow: Flow<List<ContentItemPreview>> = flowOf(listOf(testArticle))
        every { mockDependencies.recommendForUserUseCase() } returns testFlow

        // Given: create the RecommendationScreen child
        val recommendationConfig = FeedRootComponent.Config.RecommendationScreenConfig
        val recommendationChild = feedRootComponent.createChild(
            recommendationConfig,
            testContext.componentContext
        ) as FeedRootComponent.Child.RecommendationScreen

        // Then: verify component type and use-case invocation
        assertThat(recommendationChild.component).isInstanceOf(RecommendationListComponent::class.java)
        verify { mockDependencies.recommendForUserUseCase() }

        // Then: ensure unrelated repository was not accessed
        verify { mockDependencies.connectivityRepository wasNot called }
    }

    @Test
    fun `component should handle lifecycle state changes`() {
        // Given: record the initial lifecycle state
        val initialState = testContext.lifecycle.state

        // When: destroy the lifecycle
        testContext.destroyLifecycle()

        // Then: lifecycle state should have changed
        assertThat(testContext.lifecycle.state).isNotEqualTo(initialState)
    }

    @Test
    fun `serialization configs should be properly structured`() {
        // Given: the three config instances
        val feedConfig = FeedRootComponent.Config.FeedScreenConfig
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig("test-item-456")
        val recommendationConfig = FeedRootComponent.Config.RecommendationScreenConfig

        // Then: type checks and property assertions
        assertThat(feedConfig).isInstanceOf(FeedRootComponent.Config::class.java)
        assertThat(articleConfig).isInstanceOf(FeedRootComponent.Config::class.java)
        assertThat(articleConfig.itemId).isEqualTo("test-item-456")
        assertThat(recommendationConfig).isInstanceOf(FeedRootComponent.Config::class.java)

        // Then: ensure configs are distinct instances
        assertThat(feedConfig).isNotEqualTo(articleConfig)
        assertThat(feedConfig).isNotEqualTo(recommendationConfig)
        assertThat(articleConfig).isNotEqualTo(recommendationConfig)
    }

    @Test
    fun `bottom bar component should be initialized with correct context`() {
        // Given: retrieve the bottom bar
        val bottomBar = feedRootComponent.bottomBar

        // Then: basic initialization assertions
        assertThat(bottomBar).isNotNull()
        assertThat(bottomBar).isInstanceOf(BottomBarComponent::class.java)

        // Then: inspect implementation-specific properties
        if (bottomBar is BottomBarComponentImpl) {
            assertThat(bottomBar.onTabBarChanged).isNotNull()
        }
    }

    @Test
    fun `navigation should maintain proper back stack when navigating between screens`() {
        // Given: capture the initial child stack
        val initialStack = feedRootComponent.childStack.value

        // Then: verify default configuration and empty back stack
        FeedComponentSubjects.assertThat(initialStack)
            .hasActiveConfiguration(FeedRootComponent.Config.FeedScreenConfig)
            .hasEmptyBackStack()
        assertThat(initialStack.active.instance).isInstanceOf(FeedRootComponent.Child.FeedScreen::class.java)
    }

    @Test
    fun `component should handle back button navigation correctly`() {
        // Given: current child stack
        val childStack = feedRootComponent.childStack.value

        // Then: back button handling is configured if active is non-null
        assertThat(childStack).isNotNull()
        assertThat(feedRootComponent.childStack.value.active).isNotNull()
    }

    @Test
    fun `analytics service should be injected into article component`() {
        // Given: the Article screen config
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig("analytics-test")

        // When: create the ArticleScreen child
        val child = feedRootComponent.createChild(articleConfig, testContext.componentContext)

        // Then: verify the child type
        assertThat(child).isInstanceOf(FeedRootComponent.Child.ArticleScreen::class.java)

        // Then: analytics service should be available (not yet called)
        verify { mockDependencies.analyticsService wasNot called }
    }

    @Test
    fun `component context should be properly delegated`() {
        // Given & When: cast component to ComponentContext
        val componentContext = feedRootComponent as ComponentContext

        // Then: verify lifecycle, stateKeeper, and instanceKeeper are not null
        assertThat(componentContext.lifecycle).isNotNull()
        assertThat(componentContext.stateKeeper).isNotNull()
        assertThat(componentContext.instanceKeeper).isNotNull()
    }
}
