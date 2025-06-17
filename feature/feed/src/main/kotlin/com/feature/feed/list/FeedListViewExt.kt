package com.feature.feed.list

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.core.domain.model.ContentItemPreview
import com.feature.feed.R
import com.ndev.android.ui.shimmer.ShimmerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalDecomposeApi::class)
fun ViewContext.FeedListView(
    component: FeedListComponent
): View {
    val view = layoutInflater.inflate(R.layout.feed_list, parent, false)

    val shimmerView = view.findViewById<ShimmerView>(R.id.shimmerView)
    val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
    val errorContainer = view.findViewById<LinearLayout>(R.id.errorContainer)
    val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerFeed)
    val textError = view.findViewById<TextView>(R.id.textError)

    recyclerView.layoutManager = LinearLayoutManager(view.context)

    val adapter = object : PagingDataAdapter<ContentItemPreview, ArticleViewHolder>(DIFF_CALLBACK) {
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
    // Более правильный способ через LoadState
    adapter.addLoadStateListener { loadState ->
        val isEmpty = loadState.refresh is LoadState.NotLoading
                && loadState.append.endOfPaginationReached
                && adapter.itemCount == 0
        if ( loadState.refresh is LoadState.Loading){
            swipeRefreshLayout.visibility = View.GONE
            errorContainer.visibility = View.GONE
        }
        else if (isEmpty) {
            swipeRefreshLayout.visibility = View.GONE
            if (component.isOnline) {
                errorContainer.visibility = View.GONE
            } else {
                errorContainer.visibility = View.VISIBLE
                shimmerView.apply {
                    stopShimmer()
                    visibility = View.GONE
                }
            }
        } else {
            errorContainer.visibility = View.GONE
            swipeRefreshLayout.visibility = View.VISIBLE
            shimmerView.apply {
                stopShimmer()
                visibility = View.GONE
            }
        }

    }
    recyclerView.adapter = adapter

    // Setting up the listener for attaching/unpinning the View
    view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
        private val scope = MainScope()
        private var submitJob: Job? = null

        override fun onViewAttachedToWindow(v: View) {
            component.pagingItems.subscribe { pagingData ->
                submitJob?.cancel() // Отменить предыдущую задачу
                submitJob = scope.launch {
                    while (true) {
                        val lifecycleOwner = v.findViewTreeLifecycleOwner()
                        if (lifecycleOwner != null) {
                            adapter.submitData(lifecycleOwner.lifecycle, pagingData)
                            break
                        }
                        delay(50) // Проверяем каждые 100 мс
                    }
                }
            }
        }

        override fun onViewDetachedFromWindow(v: View) {
            scope.cancel() // Отменить все корутины
            submitJob = null
            v.removeOnAttachStateChangeListener(this)
        }
    })

    val hideRunnable = Runnable {
        textError.animate()
            .alpha(0f)
            .setDuration(1000L)
            .withEndAction {
                textError.visibility = View.GONE
                textError.alpha = 1f // сбрасываем на будущее
            }
    }


    component.isRefreshing.subscribe { isRefreshing ->
        swipeRefreshLayout.isRefreshing = isRefreshing is FeedListComponent.State.IsRefreshing
        textError.apply {
            if (isRefreshing is FeedListComponent.State.ErrorRefresh) {
                text = isRefreshing.errorMessage
                visibility = View.VISIBLE
                alpha = 1f

                // Убираем предыдущие отложенные действия, если они были
                removeCallbacks(hideRunnable)

                // Планируем скрытие через 5 секунд с анимацией
                postDelayed(hideRunnable, 5_000)
            } else {
                visibility = View.GONE
                alpha = 1f
                removeCallbacks(hideRunnable)
            }
        }
    }

    swipeRefreshLayout.setOnRefreshListener {
        component.onRefresh()
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
