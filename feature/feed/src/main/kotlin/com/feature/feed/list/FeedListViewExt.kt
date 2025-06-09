package com.feature.feed.list

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.core.domain.model.ContentItem
import com.feature.feed.R

@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.FeedListView(
    component: FeedListComponent
): View {
    val view = layoutInflater.inflate(R.layout.feed_list, parent, false)

    val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerFeed)

    recyclerView.layoutManager = LinearLayoutManager(view.context)

    val adapter = object : PagingDataAdapter<ContentItem, ArticleViewHolder>(DIFF_CALLBACK) {
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
                if (item is ContentItem.Article) {
                    holder.bind(item)
                }
            }
        }
    }
    recyclerView.adapter = adapter

    // Setting up the listener for attaching/unpinning the View
    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            component.pagingItems.subscribe { pagingData ->
                adapter.submitData(
                    v.findViewTreeLifecycleOwner()!!.lifecycle,
                    pagingData
                )
            }
        }

        override fun onViewDetachedFromWindow(v: View) {
            v.removeOnAttachStateChangeListener(this)
        }
    })

    component.isRefreshing.subscribe { isRefreshing ->
        swipeRefreshLayout.isRefreshing = isRefreshing
    }

    swipeRefreshLayout.setOnRefreshListener {
        component.onRefresh()
    }

    return view
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ContentItem>() {
    override fun areItemsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ContentItem, newItem: ContentItem): Boolean =
        oldItem == newItem
}
