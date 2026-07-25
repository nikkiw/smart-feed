package com.feature.feed.component.list.ui.compose

import android.content.Context
import android.widget.ImageView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test

class ArticleCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyImageUrlClearsTheImageRequest() {
        val imageLoader = RecordingImageLoader()

        composeRule.setContent {
            ArticleCard(
                model = article(imageUrl = ""),
                imageLoader = imageLoader,
                onClick = {},
            )
        }
        composeRule.waitForIdle()

        assertThat(imageLoader.sources.last()).isSameInstanceAs(ImageSource.Empty)
    }

    @Test
    fun removingCardClearsTheImageRequest() {
        val imageLoader = RecordingImageLoader()
        val showCard = mutableStateOf(true)

        composeRule.setContent {
            if (showCard.value) {
                ArticleCard(
                    model = article(imageUrl = "https://example.com/article.png"),
                    imageLoader = imageLoader,
                    onClick = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle { showCard.value = false }
        composeRule.waitForIdle()

        assertThat(imageLoader.sources.last()).isSameInstanceAs(ImageSource.Empty)
    }

    private fun article(imageUrl: String) =
        ArticleCardUiModel(
            id = "article-id",
            imageUrl = imageUrl,
            title = "Article title",
            shortDescription = "Article description",
            date = "2026-07-24",
            tags = listOf("compose"),
        )

    private class RecordingImageLoader : ImageLoader {
        val sources = mutableListOf<ImageSource>()

        override fun load(
            context: Context,
            imageSource: ImageSource,
            imageView: ImageView,
            options: ImageOptions,
        ) {
            sources += imageSource
        }

        override fun preload(
            context: Context,
            imageSource: ImageSource,
            options: ImageOptions,
        ) = Unit
    }
}
