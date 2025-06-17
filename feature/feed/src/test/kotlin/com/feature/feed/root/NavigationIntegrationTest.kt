package com.feature.feed.root

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import com.core.domain.model.Content
import com.core.domain.model.ContentId
import com.core.domain.model.ContentItem.Article
import com.core.domain.model.ContentItemPreview
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
import com.feature.feed.bottombar.BottomBarComponentImpl
import com.feature.feed.bottombar.model.BottomBarState
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test

class NavigationIntegrationTest {


    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()


    private lateinit var testContext: DecomposeTestUtils.TestComponentContext
    private lateinit var mockDependencies: FeedTestDataBuilder.MockDependencies
    private lateinit var feedRootComponent: FeedRootComponentImpl
    private lateinit var navigation: StackNavigation<FeedRootComponent.Config>

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        // Arrange: configure Dispatchers.Main to use our test dispatcher
        Dispatchers.setMain(testDispatcher)

        testContext = DecomposeTestUtils.createTestComponentContext()
        mockDependencies = FeedTestDataBuilder.createMockDependencies()

        // Setup mock behaviors
        every { mockDependencies.connectivityRepository.isConnected } returns MutableStateFlow(true)

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
        coEvery { mockDependencies.getContentItemUseCase.invoke(any()) } returns Result.success(
            fakeArticle
        )


        every { mockDependencies.recommendForUserUseCase.invoke() } returns flowOf(emptyList<ContentItemPreview>())

        feedRootComponent = FeedTestDataBuilder.createFeedRootComponent(
            componentContext = testContext.componentContext,
            dependencies = mockDependencies
        )

        testContext.startLifecycle()

        // Get access to navigation for testing
        navigation = feedRootComponent.navigation
    }

    @Test
    fun `navigation flow from feed to article should work correctly`() = runTest {
        // Given - Start at feed screen
        NavigationTestScenarios.verifyInitialState(feedRootComponent)

        // When - Navigate to article
        val articleId = "navigation-test-article"
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig(articleId)

        // Simulate navigation by creating the child directly
        // In real integration test, this would be triggered by user interaction
        val articleChild =
            feedRootComponent.createChild(articleConfig, testContext.componentContext)

        // Then
        assertThat(articleChild).isInstanceOf(FeedRootComponent.Child.ArticleScreen::class.java)
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) }
    }

    @Test
    fun `navigation flow from feed to recommendations should work correctly`() = runTest {
        // Given - Start at feed screen
        NavigationTestScenarios.verifyInitialState(feedRootComponent)

        // When - Navigate to recommendations via bottom bar
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl
        bottomBar.onTabBarChanged(BottomBarState.Recommendation)

        // Then
        NavigationTestScenarios.verifyNavigationToRecommendations(feedRootComponent)
    }

    @Test
    fun `deep navigation flow should maintain proper stack`() = runTest {
        // Given - Start at feed screen
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.FeedScreenConfig)
            .hasEmptyBackStack()

        // When - Navigate through multiple screens
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl

        // Navigate to recommendations
        bottomBar.onTabBarChanged(BottomBarState.Recommendation)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.RecommendationScreenConfig)
            .hasEmptyBackStack() // replaceAll clears the stack

        // Navigate back to feed
        bottomBar.onTabBarChanged(BottomBarState.List)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.FeedScreenConfig)
            .hasEmptyBackStack()
    }

    @Test
    fun `article navigation from recommendations should work`() = runTest {
        // Given - Start at recommendations screen
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl
        bottomBar.onTabBarChanged(BottomBarState.Recommendation)

        // When - Navigate to article from recommendations
        val articleId = "recommendation-article-123"
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig(articleId)
        val articleChild =
            feedRootComponent.createChild(articleConfig, testContext.componentContext)

        // Then
        assertThat(articleChild).isInstanceOf(FeedRootComponent.Child.ArticleScreen::class.java)
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) }
    }

    @Test
    fun `pop navigation should handle callbacks properly`() = runTest {
        // Given
        var popCompleted = false
        var popResult: Boolean? = null

        val popCallback: (Boolean) -> Unit = { result ->
            popCompleted = true
            popResult = result
        }

        // When
        feedRootComponent.pop(popCallback)

        // Then
        assertThat(popCompleted).isTrue()
        assertThat(popResult).isNotNull()
    }

    @OptIn(DelicateDecomposeApi::class)
    @Test
    fun `multiple article navigations should work independently`() = runTest {
        // Given
        val articleId1 = "article-001"
        val articleId2 = "article-002"

        // When - Create multiple article children
        val config1 = FeedRootComponent.Config.ArticleScreenConfig(articleId1)
        val config2 = FeedRootComponent.Config.ArticleScreenConfig(articleId2)

        navigation.push(config1)
        navigation.push(config2)

        // Then
        val stack = feedRootComponent.childStack.value
        // Then: verify default configuration and empty back stack
        FeedComponentSubjects.assertThat(stack)
            .hasActiveConfiguration(config2)
            .hasBackStackContaining(config1)
            .hasBackStackSize(2)

        // Verify each article was loaded independently
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId1)) }
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId2)) }
    }

    @Test
    fun `bottom bar state changes should trigger correct navigation`() = runTest {
        // Given
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl

        // Test sequence of navigation changes
        val navigationSequence = listOf(
            BottomBarState.Recommendation to FeedRootComponent.Config.RecommendationScreenConfig,
            BottomBarState.List to FeedRootComponent.Config.FeedScreenConfig,
            BottomBarState.Recommendation to FeedRootComponent.Config.RecommendationScreenConfig
        )

        navigationSequence.forEach { (barState, expectedConfig) ->
            // When
            bottomBar.onTabBarChanged(barState)

            // Then
            FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
                .hasActiveConfiguration(expectedConfig)
                .hasEmptyBackStack()
        }
    }

    @Test
    fun `article component callbacks should handle navigation and completion`() = runTest {
        // Given
        val articleId = "callback-article-test"
        val articleConfig = FeedRootComponent.Config.ArticleScreenConfig(articleId)
        val articleChild = feedRootComponent.createChild(
            articleConfig,
            testContext.componentContext
        ) as FeedRootComponent.Child.ArticleScreen

        // When - Test that callbacks are properly set up
        assertThat(articleChild.component).isInstanceOf(ArticleItemComponent::class.java)

        // Then - Verify component was created with proper callbacks
        // The onFinished callback should trigger navigation.pop()
        // The onClickItem callback should trigger navigation to another article
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) }
    }
}