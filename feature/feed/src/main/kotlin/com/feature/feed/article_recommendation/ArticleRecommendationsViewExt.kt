package com.feature.feed.article_recommendation

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.core.domain.model.ContentItemPreview
import com.core.image.ImageLoader
import com.core.image.ImageOptions
import com.core.image.ImageSource
import com.feature.feed.R


@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.ArticleRecommendationsView(
    component: ArticleRecommendationsComponent,
    imageLoader: ImageLoader
): View {
    // Inflate контейнер
    val root = layoutInflater.inflate(R.layout.article_recommendations_view, parent, false)
    val container = root.findViewById<LinearLayout>(R.id.containerRecommendations)

    // Подписываемся на список рекомендаций
    component.items.subscribe { list ->
        // Очищаем старые элементы
        container.removeAllViews()

        list.forEach { preview ->
            // Inflate элемента
            val itemView = layoutInflater.inflate(R.layout.item_article_recommendation, container, false)
            val ivImage = itemView.findViewById<ImageView>(R.id.ivMainImage)
            val tvTitle = itemView.findViewById<TextView>(R.id.tvTitle)
            val tvShort = itemView.findViewById<TextView>(R.id.tvShortDesc)
            val tvDate = itemView.findViewById<TextView>(R.id.tvDate)
            val tvTags = itemView.findViewById<TextView>(R.id.tvTags)

            // Заполняем данные
            imageLoader.load(
                context = ivImage.context,
                imageSource = ImageSource.Url(preview.mainImageUrl.value),
                imageView = ivImage,
                options = ImageOptions(
                    isCenterCrop = true
                )
            )

            when (preview) {
                is ContentItemPreview.ArticlePreview -> {
                    tvTitle.text = preview.title.value
                    tvShort.text = preview.short.value
                }

                is ContentItemPreview.UnknownPreview -> {
                    tvTitle.text = preview.rawType
                    tvShort.text = ""
                }
            }
            tvDate.text = preview.updatedAt.toString()  // ISO-строка
            tvTags.text = preview.tags.value.joinToString(", ")

            // Обработка клика
            itemView.setOnClickListener {
                component.onListItemClick(preview.id)
            }
            // Добавляем в контейнер
            container.addView(itemView)
        }
    }

    return root
}
