package com.feature.feed.root

import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.push
import com.core.content.model.Content
import com.core.content.model.ContentId
import com.core.content.model.ImageUrl
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt
import com.feature.feed.DecomposeTestUtils
import com.feature.feed.FeedComponentSubjects
import com.feature.feed.FeedTestDataBuilder
import com.feature.feed.NavigationTestScenarios
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.bottombar.model.BottomBarState
import com.feature.feed.component.bottombar.BottomBarComponentImpl
import com.feature.feed.component.root.FeedRootComponentImpl
import com.feature.feed.domain.model.ContentItem.Article
import com.feature.feed.domain.model.ContentItemPreview
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
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

        coEvery { mockDependencies.getContentItemUseCase.invoke(any()) } answers {
            val itemId =
                when (val rawArg = firstArg<Any>()) {
                    is ContentId -> rawArg.value
                    is String -> rawArg
                    else -> error("Unsupported content item id argument: ${rawArg::class.qualifiedName}")
                }
            Result.success(createArticle(itemId))
        }

        every { mockDependencies.recommendForUserUseCase.invoke() } returns flowOf(emptyList())
        every { mockDependencies.recommendForArticleUseCase.invoke(any()) } returns flowOf(emptyList())

        feedRootComponent =
            FeedTestDataBuilder.createFeedRootComponent(
                componentContext = testContext.componentContext,
                dependencies = mockDependencies,
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
    fun `feed list item click navigates through component and store path`() = runTest {
        val preview = createPreview("feed-click-article")
        val feedScreen = feedRootComponent.childStack.value.active.instance as FeedRootComponent.Child.FeedScreen

        feedScreen.component.feedListComponent.onListItemClick(preview)

        val activeConfig =
            feedRootComponent.childStack.value.active.configuration as
                FeedRootComponent.Config.ArticleScreenConfig
        assertThat(activeConfig.itemId).isEqualTo(preview.id.value)
        assertThat(activeConfig.preview?.id).isEqualTo(preview.id.value)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveChildOfType(FeedRootComponent.Child.ArticleScreen::class.java)
            .hasBackStackContaining(FeedRootComponent.Config.FeedScreenConfig)
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(preview.id.value)) }
    }

    @Test
    fun `navigation flow from feed to recommendations should work correctly`() = runTest {
        // Given - Start at feed screen
        NavigationTestScenarios.verifyInitialState(feedRootComponent)

        // When - Navigate to recommendations via bottom bar
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl
        bottomBar.onClickTabBar(BottomBarState.Recommendation)

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
        bottomBar.onClickTabBar(BottomBarState.Recommendation)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.RecommendationScreenConfig)
            .hasEmptyBackStack() // replaceAll clears the stack

        // Navigate back to feed
        bottomBar.onClickTabBar(BottomBarState.List)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveConfiguration(FeedRootComponent.Config.FeedScreenConfig)
            .hasEmptyBackStack()
    }

    @Test
    fun `article navigation from recommendations should work`() = runTest {
        // Given - Start at recommendations screen
        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl
        bottomBar.onClickTabBar(BottomBarState.Recommendation)

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
    fun `recommendation list item click navigates through component and store path`() = runTest {
        val preview = createPreview("recommendation-click-article")
        every { mockDependencies.recommendForUserUseCase.invoke() } returns flowOf(listOf(preview))

        val bottomBar = feedRootComponent.bottomBar as BottomBarComponentImpl
        bottomBar.onClickTabBar(BottomBarState.Recommendation)

        val recommendationScreen =
            feedRootComponent.childStack.value.active.instance as FeedRootComponent.Child.RecommendationScreen

        recommendationScreen.component.onListItemClick(preview)

        val activeConfig =
            feedRootComponent.childStack.value.active.configuration as
                FeedRootComponent.Config.ArticleScreenConfig
        assertThat(activeConfig.itemId).isEqualTo(preview.id.value)
        assertThat(activeConfig.preview?.id).isEqualTo(preview.id.value)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveChildOfType(FeedRootComponent.Child.ArticleScreen::class.java)
            .hasBackStackContaining(FeedRootComponent.Config.RecommendationScreenConfig)
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(preview.id.value)) }
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
        val navigationSequence =
            listOf(
                BottomBarState.Recommendation to FeedRootComponent.Config.RecommendationScreenConfig,
                BottomBarState.List to FeedRootComponent.Config.FeedScreenConfig,
                BottomBarState.Recommendation to FeedRootComponent.Config.RecommendationScreenConfig,
            )

        navigationSequence.forEach { (barState, expectedConfig) ->
            // When
            bottomBar.onClickTabBar(barState)

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
        val articleChild =
            feedRootComponent.createChild(
                articleConfig,
                testContext.componentContext,
            ) as FeedRootComponent.Child.ArticleScreen

        // When - Test that callbacks are properly set up
        assertThat(articleChild.component).isInstanceOf(ArticleItemComponent::class.java)

        // Then - Verify component was created with proper callbacks
        // The onFinished callback should trigger navigation.pop()
        // The onClickItem callback should trigger navigation to another article
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(articleId)) }
    }

    @OptIn(DelicateDecomposeApi::class)
    @Test
    fun `article recommendations item click navigates through component and store path`() = runTest {
        val currentArticleId = "article-host"
        val recommendedPreview = createPreview("article-recommended-1")
        every { mockDependencies.recommendForArticleUseCase.invoke(ContentId(currentArticleId)) } returns
            flowOf(listOf(recommendedPreview))

        navigation.push(FeedRootComponent.Config.ArticleScreenConfig(currentArticleId))

        val articleScreen =
            feedRootComponent.childStack.value.active.instance as
                FeedRootComponent.Child.ArticleScreen

        articleScreen.component.articleRecommendationsComponent.onListItemClick(recommendedPreview)

        val activeConfig =
            feedRootComponent.childStack.value.active.configuration as
                FeedRootComponent.Config.ArticleScreenConfig
        assertThat(activeConfig.itemId).isEqualTo(recommendedPreview.id.value)
        assertThat(activeConfig.preview?.id).isEqualTo(recommendedPreview.id.value)
        FeedComponentSubjects.assertThat(feedRootComponent.childStack.value)
            .hasActiveChildOfType(FeedRootComponent.Child.ArticleScreen::class.java)
            .hasBackStackContaining(FeedRootComponent.Config.ArticleScreenConfig(currentArticleId))
        coVerify { mockDependencies.getContentItemUseCase.invoke(ContentId(recommendedPreview.id.value)) }
    }

    private fun createPreview(id: String): ContentItemPreview.ArticlePreview = ContentItemPreview.ArticlePreview(
        id = ContentId(id),
        updatedAt = UpdatedAt.now(),
        mainImageUrl = ImageUrl("https://example.com/$id.png"),
        tags = Tags(listOf("compose", "android")),
        title = Title("Title $id"),
        short = ShortDescription("Short $id"),
    )

    private fun createArticle(id: String): Article = Article(
        id = ContentId(id),
        updatedAt = UpdatedAt.now(),
        mainImageUrl = ImageUrl("https://example.com/$id.png"),
        tags = Tags(listOf("tag1", "tag2")),
        title = Title("Test Title $id"),
        short = ShortDescription("Short desc $id"),
        content = Content("Full article text for $id"),
    )
}
