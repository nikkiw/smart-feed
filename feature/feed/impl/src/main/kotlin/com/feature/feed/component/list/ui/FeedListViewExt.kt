package com.feature.feed.component.list.ui

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnAttach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.arkivanov.decompose.value.subscribe
import com.feature.feed.R
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.list.FeedListComponent
import com.feature.feed.ui.openInternetSettings
import com.google.android.material.button.MaterialButton
import com.ndev.android.ui.shimmer.ShimmerView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalDecomposeApi::class)
@Suppress("FunctionName")
fun ViewContext.FeedListView(
    component: FeedListComponent,
    articleCardRenderMode: ArticleCardRenderMode,
): View {
    val view = layoutInflater.inflate(R.layout.feed_list, parent, false)
    val initialLoadingContainer = view.findViewById<View>(R.id.initialLoadingContainer)
    val shimmer = view.findViewById<ShimmerView>(R.id.shimmerView)
    val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
    val errorContainer = view.findViewById<LinearLayout>(R.id.errorContainer)
    val recycler = view.findViewById<RecyclerView>(R.id.recyclerFeed)
    val message = view.findViewById<TextView>(R.id.errorMessage)
    val refreshError = view.findViewById<TextView>(R.id.textError)
    val action = view.findViewById<MaterialButton>(R.id.errorAction)

    recycler.layoutManager = LinearLayoutManager(view.context)
    val adapter = feedAdapter(component, articleCardRenderMode)
    recycler.adapter = adapter

    val renderer =
        FeedRenderer(
            views =
                FeedViews(
                    initialLoadingContainer,
                    shimmer,
                    swipeRefresh,
                    errorContainer,
                    recycler,
                    message,
                    refreshError,
                    action,
                ),
            adapter = adapter,
            component = component,
        )

    adapter.addLoadStateListener(renderer::onLoadStates)
    component.model.subscribe(lifecycle) { model -> renderer.onModel(model) }
    view.doOnAttach {
        val owner = it.findViewTreeLifecycleOwner() ?: return@doOnAttach
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { component.pagingItems.collectLatest(adapter::submitData) }
                launch {
                    component.effects.collect { effect ->
                        if (effect == FeedListComponent.Effect.OpenInternetSettings) {
                            openInternetSettings(view.context)
                        }
                    }
                }
            }
        }
    }
    swipeRefresh.setOnRefreshListener(component::onRefresh)
    installBottomBarScrollBehavior(recycler)
    return view
}

private data class FeedViews(
    val initialLoadingContainer: View,
    val shimmer: ShimmerView,
    val swipeRefresh: SwipeRefreshLayout,
    val stateContainer: LinearLayout,
    val recycler: RecyclerView,
    val message: TextView,
    val refreshError: TextView,
    val action: MaterialButton,
)

private class FeedRenderer(
    private val views: FeedViews,
    private val adapter: PagingDataAdapter<*, *>,
    private val component: FeedListComponent,
) {
    private var model = component.model.value
    private var loadStates: CombinedLoadStates? = null
    private val initialLoadTracker = InitialPagingLoadTracker()

    fun onModel(model: FeedListComponent.Model) {
        this.model = model
        render()
    }

    fun onLoadStates(loadStates: CombinedLoadStates) {
        this.loadStates = loadStates
        initialLoadTracker.onRefreshState(loadStates.refresh)
        render()
    }

    private fun render() {
        val presentation =
            resolveFeedPresentation(
                model = model,
                loadStates = loadStates ?: return,
                itemCount = adapter.itemCount,
                isInitialLoadPending = initialLoadTracker.isPending,
            )
        val refreshFailure = model.refreshState as? FeedListComponent.RefreshState.Failed
        views.swipeRefresh.isRefreshing = model.refreshState is FeedListComponent.RefreshState.Refreshing
        views.refreshError.text = refreshFailure?.message.orEmpty()
        views.refreshError.visibility =
            if (refreshFailure != null && presentation is FeedPresentation.Content) View.VISIBLE else View.GONE
        views.initialLoadingContainer.visibility =
            if (presentation is FeedPresentation.InitialLoading) View.VISIBLE else View.GONE
        if (presentation !is FeedPresentation.InitialLoading) views.shimmer.stopShimmer()
        views.recycler.visibility = if (presentation is FeedPresentation.Content) View.VISIBLE else View.GONE
        views.swipeRefresh.visibility = if (presentation is FeedPresentation.Content) View.VISIBLE else View.GONE
        views.stateContainer.visibility =
            if (presentation is FeedPresentation.Offline ||
                presentation is FeedPresentation.Error ||
                presentation is FeedPresentation.Empty
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
        renderState(presentation)
    }

    private fun renderState(presentation: FeedPresentation) {
        views.action.visibility = View.VISIBLE
        when (presentation) {
            FeedPresentation.Offline -> {
                views.message.setText(R.string.error_loading_data)
                views.action.setText(R.string.enable_internet)
                views.action.setOnClickListener { component.onEnableInternetClicked() }
            }

            is FeedPresentation.Error -> {
                views.message.text = presentation.message
                views.action.setText(R.string.retry)
                views.action.setOnClickListener { component.onRetry() }
            }

            FeedPresentation.Empty -> {
                views.message.setText(R.string.feed_empty)
                views.action.visibility = View.GONE
            }

            FeedPresentation.Content,
            FeedPresentation.InitialLoading,
            -> Unit
        }
    }
}

internal class InitialPagingLoadTracker {
    var isPending: Boolean = true
        private set

    private var hasStarted = false

    fun onRefreshState(loadState: LoadState) {
        when (loadState) {
            LoadState.Loading -> hasStarted = true
            is LoadState.Error,
            is LoadState.NotLoading,
            -> if (hasStarted) isPending = false
        }
    }
}

internal sealed interface FeedPresentation {
    data object InitialLoading : FeedPresentation

    data object Content : FeedPresentation

    data object Offline : FeedPresentation

    data object Empty : FeedPresentation

    data class Error(val message: String) : FeedPresentation
}

internal fun resolveFeedPresentation(
    model: FeedListComponent.Model,
    loadStates: CombinedLoadStates,
    itemCount: Int,
    isInitialLoadPending: Boolean,
): FeedPresentation {
    val refreshError = (model.refreshState as? FeedListComponent.RefreshState.Failed)?.message
    val pagingError = (loadStates.refresh as? LoadState.Error)?.error?.message
    return when {
        itemCount > 0 -> FeedPresentation.Content
        isInitialLoadPending -> FeedPresentation.InitialLoading
        loadStates.refresh is LoadState.Loading -> FeedPresentation.InitialLoading
        !model.hasLocalContent && !model.isOnline -> FeedPresentation.Offline
        refreshError != null -> FeedPresentation.Error(refreshError)
        pagingError != null -> FeedPresentation.Error(pagingError)
        else -> FeedPresentation.Empty
    }
}

private fun feedAdapter(
    component: FeedListComponent,
    articleCardRenderMode: ArticleCardRenderMode,
) = object : PagingDataAdapter<ContentItemPreview, ArticleCardViewHolder>(ContentItemPreviewDiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ArticleCardViewHolder =
        ArticleCardViewHolder.create(parent, articleCardRenderMode) {
            component.onListItemClick(it.id)
        }

    override fun onBindViewHolder(
        holder: ArticleCardViewHolder,
        position: Int,
    ) {
        (getItem(position) as? ContentItemPreview.ArticlePreview)?.let(holder::bind) ?: holder.recycle()
    }

    override fun onViewRecycled(holder: ArticleCardViewHolder) {
        holder.recycle()
        super.onViewRecycled(holder)
    }
}

private fun installBottomBarScrollBehavior(recycler: RecyclerView) {
    recycler.addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int,
            ) {
                val bottomBar = recyclerView.rootView?.findViewById<View>(R.id.bottom_menu_view) ?: return
                if (dy > 0 && bottomBar.translationY == 0f) {
                    bottomBar.animate().translationY(bottomBar.height.toFloat()).setDuration(200).start()
                } else if (dy < 0 && bottomBar.translationY != 0f) {
                    bottomBar.animate().translationY(0f).setDuration(200).start()
                }
            }
        },
    )
}
