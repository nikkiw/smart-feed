package com.feature.feed.component.list.ui

import androidx.recyclerview.widget.DiffUtil
import com.feature.feed.domain.model.ContentItemPreview

internal object ContentItemPreviewDiffCallback : DiffUtil.ItemCallback<ContentItemPreview>() {
    override fun areItemsTheSame(
        oldItem: ContentItemPreview,
        newItem: ContentItemPreview,
    ): Boolean = oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ContentItemPreview,
        newItem: ContentItemPreview,
    ): Boolean =
        when (oldItem) {
            is ContentItemPreview.ArticlePreview ->
                newItem is ContentItemPreview.ArticlePreview && oldItem == newItem
            is ContentItemPreview.UnknownPreview ->
                newItem is ContentItemPreview.UnknownPreview && oldItem == newItem
        }
}
