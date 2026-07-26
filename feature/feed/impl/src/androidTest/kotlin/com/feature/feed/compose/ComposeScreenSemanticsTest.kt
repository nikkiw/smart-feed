package com.feature.feed.compose

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.core.content.model.Content
import com.core.content.model.ContentId
import com.core.content.model.ImageUrl
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt
import com.feature.feed.article.ArticleItemComponent
import com.feature.feed.articlerecommendation.ArticleRecommendationsComponent
import com.feature.feed.domain.model.ContentItem
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.recommendation.RecommendationListComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Rule
import org.junit.Test

class ComposeScreenSemanticsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun recommendationScreen_content_state_renders_heading_and_items() {
        composeRule.setContent {
            SmartFeedTheme {
                RecommendationScreen(
                    component =
                    FakeRecommendationListComponent(
                        RecommendationListComponent.Model(
                            items =
                            listOf(
                                ContentItemPreview.ArticlePreview(
                                    id = ContentId("recommendation-1"),
                                    updatedAt = UpdatedAt(1_721_894_400_000),
                                    mainImageUrl = ImageUrl(""),
                                    tags = Tags(listOf("Compose")),
                                    title = Title("Modern Compose patterns"),
                                    short = ShortDescription("State hoisting and semantics."),
                                ),
                            ),
                            isOnline = true,
                            hasLocalContent = true,
                            loadState = RecommendationListComponent.LoadState.Idle,
                        ),
                    ),
                )
            }
        }

        composeRule
            .onNodeWithText("Recommendations")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("Modern Compose patterns").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun articleScreen_content_state_exposes_back_action_and_heading() {
        composeRule.setContent {
            SmartFeedTheme {
                ArticleScreen(
                    component =
                    FakeArticleItemComponent(
                        ArticleItemComponent.Model(
                            contentState =
                            ArticleItemComponent.ContentState.Content(
                                ContentItem.Article(
                                    id = ContentId("article-1"),
                                    updatedAt = UpdatedAt(1_721_894_400_000),
                                    mainImageUrl = ImageUrl(""),
                                    tags = Tags(listOf("Compose", "Accessibility")),
                                    title = Title("Accessible Compose article"),
                                    short = ShortDescription("Preview"),
                                    content = Content("# Heading\n\nBody"),
                                ),
                            ),
                        ),
                    ),
                )
            }
        }

        composeRule
            .onNodeWithContentDescription("Back")
            .assertIsDisplayed()
        composeRule
            .onNodeWithText("Accessible Compose article")
            .assertIsDisplayed()
            .assert(SemanticsMatcher.keyIsDefined(SemanticsProperties.Heading))
        composeRule.onNodeWithText("Recommended reading").assertIsDisplayed()
    }
}

private class FakeRecommendationListComponent(
    initialModel: RecommendationListComponent.Model,
) : RecommendationListComponent {
    override val model: Value<RecommendationListComponent.Model> = MutableValue(initialModel)
    override val initialScrollPosition: RecommendationListComponent.ScrollPosition =
        RecommendationListComponent.ScrollPosition()
    override val effects: Flow<RecommendationListComponent.Effect> = emptyFlow()

    override fun onRetry() = Unit

    override fun onEnableInternetClicked() = Unit

    override fun onListItemClick(item: ContentItemPreview) = Unit

    override fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int) = Unit
}

private class FakeArticleItemComponent(
    initialModel: ArticleItemComponent.Model,
) : ArticleItemComponent {
    override val model: Value<ArticleItemComponent.Model> = MutableValue(initialModel)
    override val itemId: ContentId = ContentId("article-1")
    override val initialScrollPosition: ArticleItemComponent.ScrollPosition =
        ArticleItemComponent.ScrollPosition()
    override val articleRecommendationsComponent: ArticleRecommendationsComponent =
        FakeArticleRecommendationsComponent()

    override fun onClose() = Unit

    override fun onRetry() = Unit

    override fun onReadProgressChanged(percentRead: Float) = Unit

    override fun onScrollPositionChanged(itemIndex: Int, itemOffsetPx: Int) = Unit
}

private class FakeArticleRecommendationsComponent : ArticleRecommendationsComponent {
    override val model: Value<ArticleRecommendationsComponent.Model> =
        MutableValue(ArticleRecommendationsComponent.Model(ArticleRecommendationsComponent.State.Empty))

    override fun onRetry() = Unit

    override fun onListItemClick(item: ContentItemPreview) = Unit
}
