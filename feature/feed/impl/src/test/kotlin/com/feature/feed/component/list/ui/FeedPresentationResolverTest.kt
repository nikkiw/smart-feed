package com.feature.feed.component.list.ui

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates
import com.feature.feed.list.FeedListComponent
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FeedPresentationResolverTest {
    @Test
    fun `initial paging tracker ignores adapter idle state before first load`() {
        val tracker = InitialPagingLoadTracker()

        tracker.onRefreshState(LoadState.NotLoading(endOfPaginationReached = false))

        assertThat(tracker.isPending).isTrue()
    }

    @Test
    fun `initial paging tracker completes after real loading cycle`() {
        val tracker = InitialPagingLoadTracker()

        tracker.onRefreshState(LoadState.Loading)
        assertThat(tracker.isPending).isTrue()

        tracker.onRefreshState(LoadState.NotLoading(endOfPaginationReached = false))
        assertThat(tracker.isPending).isFalse()
    }

    @Test
    fun `initial adapter idle state stays initial loading until first paging load completes`() {
        val presentation =
            resolveFeedPresentation(
                model = model(isOnline = true, hasLocalContent = false),
                loadStates = notLoadingStates(),
                itemCount = 0,
                isInitialLoadPending = true,
            )

        assertThat(presentation).isEqualTo(FeedPresentation.InitialLoading)
    }

    @Test
    fun `resolved empty paging result renders empty state`() {
        val presentation =
            resolveFeedPresentation(
                model = model(isOnline = true, hasLocalContent = true),
                loadStates = notLoadingStates(),
                itemCount = 0,
                isInitialLoadPending = false,
            )

        assertThat(presentation).isEqualTo(FeedPresentation.Empty)
    }

    @Test
    fun `available paging items render content while initial load flag is pending`() {
        val presentation =
            resolveFeedPresentation(
                model = model(isOnline = true, hasLocalContent = true),
                loadStates = notLoadingStates(),
                itemCount = 1,
                isInitialLoadPending = true,
            )

        assertThat(presentation).isEqualTo(FeedPresentation.Content)
    }

    private fun model(
        isOnline: Boolean,
        hasLocalContent: Boolean,
    ) = FeedListComponent.Model(
        isOnline = isOnline,
        hasLocalContent = hasLocalContent,
    )

    private fun notLoadingStates(): CombinedLoadStates {
        val source =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            )
        return CombinedLoadStates(
            refresh = source.refresh,
            prepend = source.prepend,
            append = source.append,
            source = source,
            mediator = null,
        )
    }
}
