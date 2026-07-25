package com.feature.feed.component.list.ui

import android.content.Context
import android.widget.ImageView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.core.image.ImageLoader
import com.core.image.ImageSource
import com.feature.feed.component.list.ui.compose.ArticleCard
import com.feature.feed.component.list.ui.compose.ArticleCardUiModel
import com.feature.feed.component.list.ui.compose.SmartFeedMaterialTheme
import com.feature.feed.component.list.ui.compose.toArticleCardUiModel
import com.feature.feed.domain.model.ContentItemPreview

/** Compose island hosted by the existing RecyclerView. */
class ComposeArticleCardViewHolder(
    context: Context,
    onClick: (ContentItemPreview.ArticlePreview) -> Unit,
    private val imageLoader: ImageLoader,
) : ArticleCardViewHolder(ComposeView(context), onClick) {
    private val model = mutableStateOf<ArticleCardUiModel?>(null)
    private var imageView: ImageView? = null

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool,
        )
        composeView.setContent {
            SmartFeedMaterialTheme {
                model.value?.let { currentModel ->
                    ArticleCard(
                        model = currentModel,
                        imageLoader = imageLoader,
                        onClick = ::dispatchCurrentArticleClick,
                        onImageViewAttached = ::onImageViewAttached,
                        onImageViewReleased = ::onImageViewReleased,
                    )
                }
            }
        }
    }

    private val composeView: ComposeView
        get() = itemView as ComposeView

    override fun render(article: ContentItemPreview.ArticlePreview) {
        model.value = article.toArticleCardUiModel()
    }

    override fun recycle() {
        super.recycle()
        imageView?.let(::clearImageRequest)
    }

    private fun onImageViewAttached(view: ImageView) {
        imageView = view
    }

    private fun onImageViewReleased(view: ImageView) {
        if (imageView === view) {
            imageView = null
        }
    }

    private fun clearImageRequest(view: ImageView) {
        imageLoader.load(
            context = view.context,
            imageSource = ImageSource.Empty,
            imageView = view,
        )
    }
}
