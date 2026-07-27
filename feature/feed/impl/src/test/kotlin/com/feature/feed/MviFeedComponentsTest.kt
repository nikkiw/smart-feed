package com.feature.feed

import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.core.content.model.ContentId
import com.core.observers.ConnectivityRepository
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.component.article.ArticleItemComponentImpl
import com.feature.feed.component.articlerecommendation.ArticleRecommendationsComponentImpl
import com.feature.feed.component.recommendation.RecommendationListComponentImpl
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.usecase.GetContentItemUseCase
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.feature.feed.recommendation.RecommendationListComponent
import com.feature.recommendation.domain.usecase.RecommendForArticleUseCase
import com.feature.recommendation.domain.usecase.RecommendForUserUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MviFeedComponentsTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `article exposes local load failure without connectivity dependency`() = runTest(dispatcher) {
        val context = DecomposeTestUtils.createTestComponentContext()
        val getArticle = mockk<GetContentItemUseCase>()
        coEvery { getArticle(ContentId("missing")) } returns Result.failure(IllegalStateException("missing"))
        val related = mockk<RecommendForArticleUseCase>()
        every { related(any()) } returns flowOf(emptyList())
        val component =
            ArticleItemComponentImpl(
                componentContext = context.componentContext,
                storeFactory = DefaultStoreFactory(),
                getContentItemUseCase = getArticle,
                recommendForArticleUseCase = related,
                analyticsService = mockk(relaxed = true),
                itemId = ContentId("missing"),
                onFinished = {},
                onClickItem = {},
            )

        context.startLifecycle()
        advanceUntilIdle()

        assertThat(component.model.value.contentState)
            .isEqualTo(ArticleItemComponent.ContentState.Failed("missing"))
        context.destroyLifecycle()
    }

    @Test
    fun `article recommendations expose valid local empty state`() = runTest(dispatcher) {
        val context = DecomposeTestUtils.createTestComponentContext()
        val useCase = mockk<RecommendForArticleUseCase>()
        every { useCase(any()) } returns flowOf(emptyList())
        val component =
            ArticleRecommendationsComponentImpl(
                componentContext = context.componentContext,
                storeFactory = DefaultStoreFactory(),
                articleId = ContentId("article"),
                recommendForArticleUseCase = useCase,
                onItemClick = {},
            )

        context.startLifecycle()
        advanceUntilIdle()

        assertThat(component.model.value.state).isEqualTo(ArticleRecommendationsComponent.State.Empty)
        context.destroyLifecycle()
    }

    @Test
    fun `article recommendations update when room emits after initial empty state`() = runTest(dispatcher) {
        val context = DecomposeTestUtils.createTestComponentContext()
        val recommendations = MutableStateFlow(emptyList<ContentItemPreview>())
        val useCase = mockk<RecommendForArticleUseCase>()
        every { useCase(ContentId("article")) } returns recommendations
        val component =
            ArticleRecommendationsComponentImpl(
                componentContext = context.componentContext,
                storeFactory = DefaultStoreFactory(),
                articleId = ContentId("article"),
                recommendForArticleUseCase = useCase,
                onItemClick = {},
            )

        context.startLifecycle()
        advanceUntilIdle()
        assertThat(component.model.value.state).isEqualTo(ArticleRecommendationsComponent.State.Empty)

        val preview = mockk<ContentItemPreview>()
        recommendations.value = listOf(preview)
        advanceUntilIdle()

        assertThat(component.model.value.state)
            .isEqualTo(ArticleRecommendationsComponent.State.Content(listOf(preview)))
        context.destroyLifecycle()
    }

    @Test
    fun `recommendation model distinguishes empty room while offline`() = runTest(dispatcher) {
        val context = DecomposeTestUtils.createTestComponentContext()
        val connectivity = mockk<ConnectivityRepository>()
        every { connectivity.isConnected } returns MutableStateFlow(false)
        val repository = mockk<ContentItemRepository>()
        every { repository.observeHasContent() } returns flowOf(false)
        val recommendations = mockk<RecommendForUserUseCase>()
        every { recommendations() } returns flowOf(emptyList())
        val component =
            RecommendationListComponentImpl(
                componentContext = context.componentContext,
                storeFactory = DefaultStoreFactory(),
                recommendForUserUseCase = recommendations,
                connectivityRepository = connectivity,
                contentItemRepository = repository,
                syncContentUseCase = mockk<SyncContentUseCase>(relaxed = true),
                onItemClick = {},
            )

        context.startLifecycle()
        advanceUntilIdle()

        assertThat(component.model.value)
            .isEqualTo(
                RecommendationListComponent.Model(
                    isOnline = false,
                    hasLocalContent = false,
                    loadState = RecommendationListComponent.LoadState.Idle,
                ),
            )
        context.destroyLifecycle()
    }
}
