package com.feature.feed.component.recommendation

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.doOnAttach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.android.ViewContext
import com.arkivanov.decompose.extensions.android.layoutInflater
import com.arkivanov.decompose.value.subscribe
import com.feature.feed.R
import com.feature.feed.component.list.ui.ArticleCardRenderMode
import com.feature.feed.component.list.ui.ArticleCardViewHolder
import com.feature.feed.component.list.ui.ContentItemPreviewDiffCallback
import com.feature.feed.domain.model.ContentItemPreview
import com.feature.feed.recommendation.RecommendationListComponent
import com.feature.feed.ui.openInternetSettings
import com.google.android.material.button.MaterialButton
import com.ndev.android.ui.shimmer.ShimmerView
import kotlinx.coroutines.launch

@OptIn(ExperimentalDecomposeApi::class)
@Suppress("FunctionName")
fun ViewContext.RecommendationListView(
    component: RecommendationListComponent,
    articleCardRenderMode: ArticleCardRenderMode,
): View {
    val view = layoutInflater.inflate(R.layout.recommendation_list, parent, false)
    val recycler = view.findViewById<RecyclerView>(R.id.recyclerFeed)
    val shimmer = view.findViewById<ShimmerView>(R.id.shimmerView)
    val stateContainer = view.findViewById<LinearLayout>(R.id.errorContainer)
    val message = view.findViewById<TextView>(R.id.errorMessage)
    val action = view.findViewById<MaterialButton>(R.id.errorAction)
    recycler.layoutManager = LinearLayoutManager(view.context)

    val adapter = recommendationAdapter(component, articleCardRenderMode)
    recycler.adapter = adapter

    component.model.subscribe(lifecycle) { model ->
        val loading = model.loadState is RecommendationListComponent.LoadState.Loading && model.items.isEmpty()
        val offline = model.items.isEmpty() && !model.hasLocalContent && !model.isOnline
        val failure = model.loadState as? RecommendationListComponent.LoadState.Failed

        shimmer.visibility = if (loading) View.VISIBLE else View.GONE
        if (!loading) shimmer.stopShimmer()
        recycler.visibility = if (model.items.isNotEmpty()) View.VISIBLE else View.GONE
        stateContainer.visibility = if (!loading && model.items.isEmpty()) View.VISIBLE else View.GONE
        action.visibility = View.VISIBLE
        when {
            offline -> {
                message.setText(R.string.error_loading_data)
                action.setText(R.string.enable_internet)
                action.setOnClickListener { component.onEnableInternetClicked() }
            }

            failure != null -> {
                message.text = failure.message
                action.setText(R.string.retry)
                action.setOnClickListener { component.onRetry() }
            }

            else -> {
                message.setText(R.string.recommendations_empty)
                action.visibility = View.GONE
            }
        }
        adapter.submitList(model.items)
    }

    view.doOnAttach {
        val owner = it.findViewTreeLifecycleOwner() ?: return@doOnAttach
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                component.effects.collect { effect ->
                    if (effect == RecommendationListComponent.Effect.OpenInternetSettings) {
                        openInternetSettings(view.context)
                    }
                }
            }
        }
    }
    return view
}

private fun recommendationAdapter(
    component: RecommendationListComponent,
    articleCardRenderMode: ArticleCardRenderMode,
) = object : ListAdapter<ContentItemPreview, ArticleCardViewHolder>(ContentItemPreviewDiffCallback) {
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
