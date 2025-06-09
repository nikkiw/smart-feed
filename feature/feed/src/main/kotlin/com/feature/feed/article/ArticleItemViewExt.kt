package com.feature.feed.article

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.arkivanov.decompose.value.subscribe
import com.core.domain.model.ContentItem
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.ArticleItemView(
    component: ArticleItemComponent,
    imageLoader: ImageLoader
): View {
    val view = layoutInflater.inflate(R.layout.item_article_component, parent, false)
    val toolbar = view.findViewById<MaterialToolbar>(R.id.articleToolbar)

    toolbar.apply {
        setNavigationOnClickListener {
            component.onClose()
        }
    }


    val image = view.findViewById<ImageView>(R.id.articleImage)
    val title = view.findViewById<TextView>(R.id.articleTitle)
    val content = view.findViewById<TextView>(R.id.articleContent)
    val date = view.findViewById<TextView>(R.id.articleDate)
    val tagGroup = view.findViewById<ChipGroup>(R.id.articleTags)

    component.model.subscribe(lifecycle) { state ->
        when (state) {
            is ArticleItemComponent.State.Error -> {
                image.visibility = View.GONE
                title.apply {
                    text = state.errorMessage
                    visibility = View.VISIBLE
                }
                content.visibility = View.GONE
                date.visibility = View.GONE
                tagGroup.removeAllViews()
            }

            ArticleItemComponent.State.Init -> {
                image.visibility = View.GONE
                title.apply {
                    text = context.getString(R.string.article_state_is_loaded)
                    visibility = View.VISIBLE
                }
                content.visibility = View.GONE
                date.visibility = View.GONE
                tagGroup.removeAllViews()
            }

            is ArticleItemComponent.State.Loaded -> {
                (state.contentItem as? ContentItem.Article)?.let { model ->
                    toolbar.title = model.title.value

                    title.apply {
                        text = model.title.value
                        visibility = View.VISIBLE
                    }

                    content.apply {
                        text = model.content.value
                        visibility = View.VISIBLE
                    }

                    date.apply {
                        text = model.updatedAt.toString()
                        visibility = View.VISIBLE
                    }

                    image.visibility = View.VISIBLE
                    imageLoader.load(
                        context = view.context,
                        imageSource = ImageSource.Url(model.mainImageUrl.value),
                        imageView = image,
                        options = ImageOptions(
                            isCenterCrop = true
                        )
                    )


                    tagGroup.removeAllViews()
                    for (tag in model.tags.value) {
                        val chip = Chip(view.context)
                        chip.text = tag
                        chip.isCheckable = false
                        chip.isClickable = false
                        tagGroup.addView(chip)
                    }
                }
            }
        }

    }

    return view
}