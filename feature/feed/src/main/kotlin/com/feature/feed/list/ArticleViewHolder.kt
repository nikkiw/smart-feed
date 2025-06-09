package com.feature.feed.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.core.di.ImageLoaderEntryPoint
import com.core.domain.model.ContentItem
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import dagger.hilt.android.EntryPointAccessors

/**
 * The ViewHolder for the article uses the ImageLoader abstraction to load images.
 */
class ArticleViewHolder private constructor(
    itemView: View,
    private val onClick: (ContentItem.Article) -> Unit,
    private val imageLoader: ImageLoader
) : RecyclerView.ViewHolder(itemView) {

    private val image: ImageView = itemView.findViewById(R.id.articleImage)
    private val title: TextView = itemView.findViewById(R.id.articleTitle)
    private val content: TextView = itemView.findViewById(R.id.articleContent)
    private val date: TextView = itemView.findViewById(R.id.articleDate)
    private val tagGroup: ChipGroup = itemView.findViewById(R.id.articleTags)

    private var currentArticle: ContentItem.Article? = null

    init {
        itemView.setOnClickListener {
            currentArticle?.let { onClick(it) }
        }
    }

    fun bind(article: ContentItem.Article) {
        currentArticle = article
        // Uploading an image using the ImageLoader abstraction
        if (article.mainImageUrl.value.isNotEmpty()) {
            imageLoader.load(
                context = itemView.context,
                imageSource = ImageSource.Url(article.mainImageUrl.value),
                imageView = image,
                options = ImageOptions(
                    isCenterCrop = true
                )
            )
        } else {
            image.setImageDrawable(null)
        }
        title.text = article.title.value
        content.text = article.content.value
        date.text = article.updatedAt.toString()

        // Теги
        tagGroup.removeAllViews()
        for (tag in article.tags.value) {
            val chip = Chip(itemView.context)
            chip.text = tag
            chip.isCheckable = false
            chip.isClickable = false
            tagGroup.addView(chip)
        }
    }

    companion object {
        /**
         * Creates an ArticleViewHolder. Use this method in the onCreateViewHolder of the adapter.
         * Gets ImageLoader via Hilt EntryPoint.
         */
        fun create(parent: ViewGroup, onClick: (ContentItem.Article) -> Unit): ArticleViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_article, parent, false)
            // Getting ImageLoader via Hilt EntryPoint
            val imageLoader = EntryPointAccessors.fromApplication<ImageLoaderEntryPoint>(
                parent.context.applicationContext
            ).imageLoader()
            return ArticleViewHolder(view, onClick, imageLoader)
        }
    }
}
