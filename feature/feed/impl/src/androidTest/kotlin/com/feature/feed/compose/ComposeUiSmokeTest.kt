package com.feature.feed.compose

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.core.content.model.ContentId
import com.core.content.model.ImageUrl
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt
import com.feature.feed.domain.model.ContentItemPreview
import org.junit.Rule
import org.junit.Test

class ComposeUiSmokeTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_renders_message_and_action() {
        composeRule.setContent {
            SmartFeedTheme {
                EmptyState(
                    message = "No articles yet",
                    actionText = "Retry",
                    onAction = {},
                )
            }
        }

        composeRule.onNodeWithText("No articles yet").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed().assertHasClickAction()
    }

    @Test
    fun previewCard_renders_clickable_article_preview() {
        composeRule.setContent {
            SmartFeedTheme {
                PreviewCard(
                    preview =
                    ContentItemPreview.ArticlePreview(
                        id = ContentId("card-id"),
                        updatedAt = UpdatedAt(1_721_894_400_000),
                        mainImageUrl = ImageUrl(""),
                        tags = Tags(listOf("Compose", "UI")),
                        title = Title("Compose architecture"),
                        short = ShortDescription("A smoke test for the compose preview card."),
                    ),
                    onClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Compose architecture").assertIsDisplayed()
        composeRule.onNodeWithText("A smoke test for the compose preview card.").assertIsDisplayed()
        composeRule.onNodeWithText("Compose architecture").assertHasClickAction()
    }
}
