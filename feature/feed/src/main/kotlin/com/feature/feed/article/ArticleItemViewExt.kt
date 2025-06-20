package com.feature.feed.article

import android.view.View
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.child
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.arkivanov.decompose.value.subscribe
import com.core.domain.model.ContentItem
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.R
import com.feature.feed.article_recommendation.ArticleRecommendationsView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import io.noties.markwon.Markwon


@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.ArticleItemView(
    component: ArticleItemComponent,
    imageLoader: ImageLoader,
    markwon: Markwon
): View {
    val view = layoutInflater.inflate(R.layout.item_article_component, parent, false)
    val toolbar = view.findViewById<MaterialToolbar>(R.id.articleToolbar)

    toolbar.apply {
        setNavigationOnClickListener {
            component.onClose()
        }
    }


    child(view.findViewById(R.id.articleRecommendations)) {
        ArticleRecommendationsView(
            component.articleRecommendationsComponent,
            imageLoader
        )
    }

    val scrollView = view.findViewById<ScrollView>(R.id.articleScrollView).apply {
        transitionName =  "transition_content_${component.itemId}"
    }
    val allContent = view.findViewById<CardView>(R.id.contentItem)
    val image = view.findViewById<ImageView>(R.id.articleImage)
    val title = view.findViewById<TextView>(R.id.articleTitle)
    val content = view.findViewById<TextView>(R.id.articleContent)
    val date = view.findViewById<TextView>(R.id.articleDate)
    val tagGroup = view.findViewById<ChipGroup>(R.id.articleTags)


    component.registerOnCloseListener {
        val contentHeight = allContent.height
        val scrollViewHeight = scrollView.height
        val percentSeen =
            ((scrollView.scrollY + scrollViewHeight).toFloat() / contentHeight).coerceIn(
                0f,
                1f
            )

        component.logPercentRead(percentSeen)
    }

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
//                image.visibility = View.VISIBLE
//                title.apply {
//                    text = context.getString(R.string.article_state_is_loaded)
//                    visibility = View.VISIBLE
//                }
//                content.visibility = View.GONE
//                date.visibility = View.GONE
//                tagGroup.removeAllViews()
            }

            is ArticleItemComponent.State.Loaded -> {
                (state.contentItem as? ContentItem.Article)?.let { model ->
                    toolbar.title = model.title.value

                    title.apply {
                        text = model.title.value
                        visibility = View.VISIBLE
                    }

                    content.apply {
                        text = markwon.toMarkdown(model.content.value)
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
                    scrollView.setOnScrollChangeListener { v: View, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                        val contentHeight = allContent.height
                        val scrollViewHeight = scrollView.height
                        val percentSeen =
                            ((scrollY + scrollViewHeight).toFloat() / contentHeight).coerceIn(
                                0f,
                                1f
                            )

                        component.logPercentRead(percentSeen)
                    }
                }
            }
        }

    }

    return view
}