package com.feature.feed.store

import com.core.content.model.ContentType
import com.core.content.model.Tags
import com.feature.feed.component.list.store.FeedMsg
import com.feature.feed.component.list.store.FeedReducer
import com.feature.feed.component.list.store.FeedState
import com.feature.feed.domain.repository.ContentItemsSortedType
import com.feature.feed.domain.repository.Query
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FeedReducerTest {
    private val initialQuery =
        Query(listOf(ContentType.ARTICLE), Tags(listOf("kotlin")), ContentItemsSortedType.ByDateNewestFirst)
    private val updatedQuery =
        Query(listOf(ContentType.ARTICLE), Tags(listOf("compose")), ContentItemsSortedType.ByNameAsc)

    @Test
    fun `query connectivity and local availability update independently`() {
        val initial = FeedState(query = initialQuery, isOnline = false)

        val result =
            reduce(
                reduce(
                    reduce(initial, FeedMsg.QueryChanged(updatedQuery)),
                    FeedMsg.ConnectivityChanged(true),
                ),
                FeedMsg.LocalAvailabilityChanged(true),
            )

        assertThat(result.query).isEqualTo(updatedQuery)
        assertThat(result.isOnline).isTrue()
        assertThat(result.hasLocalContent).isTrue()
        assertThat(result.refreshState).isEqualTo(FeedState.RefreshState.Idle)
    }

    @Test
    fun `refresh transitions and stale results are deterministic`() {
        val initial = FeedState(query = initialQuery, isOnline = true)
        val refreshing = reduce(initial, FeedMsg.RefreshStarted(2L))

        assertThat(refreshing.refreshState).isEqualTo(FeedState.RefreshState.Refreshing(2L))
        assertThat(reduce(refreshing, FeedMsg.RefreshCompleted(1L))).isEqualTo(refreshing)
        assertThat(reduce(refreshing, FeedMsg.RefreshFailed(2L, "failed")).refreshState)
            .isEqualTo(FeedState.RefreshState.Failed(2L, "failed"))
        assertThat(reduce(refreshing, FeedMsg.RefreshCompleted(2L)).refreshState)
            .isEqualTo(FeedState.RefreshState.Idle)
    }

    private fun reduce(
        state: FeedState,
        msg: FeedMsg,
    ): FeedState = FeedReducer.run { state.reduce(msg) }
}
