package com.feature.feed.recommendation

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.core.domain.model.ContentItemPreview
import com.feature.feed.R
import com.feature.feed.list.ArticleViewHolder
import com.ndev.android.ui.shimmer.ShimmerView

@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.RecommendationListView(
    component: RecommendationListComponent
): View {
    val view = layoutInflater.inflate(R.layout.recommendation_list, parent, false)
    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerFeed)
    val shimmerView = view.findViewById<ShimmerView>(R.id.shimmerView)
    val errorContainer = view.findViewById<LinearLayout>(R.id.errorContainer)

    recyclerView.layoutManager = LinearLayoutManager(view.context)

    val adapter = object : ListAdapter<ContentItemPreview, ArticleViewHolder>(DIFF_CALLBACK) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
            return ArticleViewHolder.create(
                parent = parent,
                onClick = { article ->
                    component.onListItemClick(article.id)
                }
            )
        }

        override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
            getItem(position)?.let { item ->
                if (item is ContentItemPreview.ArticlePreview) {
                    holder.bind(item)
                }
            }
        }
    }
    recyclerView.adapter = adapter

    component.items.subscribe { items ->
        if (items.isEmpty()) {
            if (component.isOnline) {
                errorContainer.visibility = View.GONE
            } else {
                errorContainer.visibility = View.VISIBLE
                shimmerView.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
            }
            recyclerView.visibility = View.GONE
        } else {
            errorContainer.visibility = View.GONE
            shimmerView.apply {
                stopShimmer()
                visibility = View.GONE
            }
            recyclerView.visibility = View.VISIBLE
        }
        adapter.submitList(items)
    }

    return view
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContentItemPreview>() {
    override fun areItemsTheSame(
        oldItem: ContentItemPreview,
        newItem: ContentItemPreview
    ): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(
        oldItem: ContentItemPreview,
        newItem: ContentItemPreview
    ): Boolean =
        oldItem == newItem
}
