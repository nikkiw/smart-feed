package com.feature.feed.component.list.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.R
import com.feature.feed.domain.model.ContentItemPreview
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

/** Existing XML renderer, retained as the control implementation. */
class XmlArticleCardViewHolder private constructor(
    itemView: View,
    onClick: (ContentItemPreview.ArticlePreview) -> Unit,
    private val imageLoader: ImageLoader,
) : ArticleCardViewHolder(itemView, onClick) {
    private val image: ImageView = itemView.findViewById(R.id.articleImage)
    private val title: TextView = itemView.findViewById(R.id.articleTitle)
    private val content: TextView = itemView.findViewById(R.id.articleContent)
    private val date: TextView = itemView.findViewById(R.id.articleDate)
    private val tagGroup: ChipGroup = itemView.findViewById(R.id.articleTags)

    init {
        itemView.setOnClickListener { dispatchCurrentArticleClick() }
    }

    override fun render(article: ContentItemPreview.ArticlePreview) {
        if (article.mainImageUrl.value.isNotEmpty()) {
            imageLoader.load(
                context = itemView.context,
                imageSource = ImageSource.Url(article.mainImageUrl.value),
                imageView = image,
                options = ImageOptions(isCenterCrop = true),
            )
        } else {
            image.setImageDrawable(null)
        }

        title.text = article.title.value
        content.text = article.short.value
        date.text = article.updatedAt.toString()

        tagGroup.removeAllViews()
        article.tags.value.forEach { tag ->
            tagGroup.addView(
                Chip(itemView.context).apply {
                    text = tag
                    isCheckable = false
                    isClickable = false
                },
            )
        }
    }

    override fun recycle() {
        super.recycle()
        image.setImageDrawable(null)
        title.text = null
        content.text = null
        date.text = null
        tagGroup.removeAllViews()
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onClick: (ContentItemPreview.ArticlePreview) -> Unit,
            imageLoader: ImageLoader,
        ): XmlArticleCardViewHolder {
            val view =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_article, parent, false)
            return XmlArticleCardViewHolder(view, onClick, imageLoader)
        }
    }
}
