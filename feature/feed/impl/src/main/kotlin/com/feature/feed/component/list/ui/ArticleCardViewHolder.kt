package com.feature.feed.component.list.ui

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.core.di.ImageLoaderEntryPoint
import com.feature.feed.domain.model.ContentItemPreview
import dagger.hilt.android.EntryPointAccessors

/** Common recycling contract for the XML control path and the Compose island. */
abstract class ArticleCardViewHolder(
    itemView: View,
    private val onClick: (ContentItemPreview.ArticlePreview) -> Unit,
) : RecyclerView.ViewHolder(itemView) {
    private var currentArticle: ContentItemPreview.ArticlePreview? = null

    fun bind(article: ContentItemPreview.ArticlePreview) {
        currentArticle = article
        itemView.transitionName = "transition_content_${article.id}"
        render(article)
    }

    open fun recycle() {
        currentArticle = null
    }

    protected fun dispatchCurrentArticleClick() {
        currentArticle?.let(onClick)
    }

    protected abstract fun render(article: ContentItemPreview.ArticlePreview)

    companion object {
        fun create(
            parent: ViewGroup,
            rendererMode: ArticleCardRenderMode,
            onClick: (ContentItemPreview.ArticlePreview) -> Unit,
        ): ArticleCardViewHolder {
            val imageLoader =
                EntryPointAccessors.fromApplication<ImageLoaderEntryPoint>(
                    parent.context.applicationContext,
                ).imageLoader()
            return when (rendererMode) {
                ArticleCardRenderMode.Xml -> XmlArticleCardViewHolder.create(parent, onClick, imageLoader)
                ArticleCardRenderMode.Compose -> ComposeArticleCardViewHolder(parent.context, onClick, imageLoader)
            }
        }
    }
}
