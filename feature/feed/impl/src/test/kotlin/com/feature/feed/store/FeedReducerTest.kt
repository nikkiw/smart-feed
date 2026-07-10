package com.feature.feed.store

import com.core.content.model.ContentId
import com.core.content.model.ContentType
import com.core.content.model.Tags
import com.feature.feed.domain.repository.ContentItemsSortedType
import com.feature.feed.domain.repository.Query
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedReducerTest {
    private val initialQuery =
        Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(listOf("kotlin")),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst,
        )

    private val updatedQuery =
        Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(listOf("compose")),
            sortedBy = ContentItemsSortedType.ByNameAsc,
        )

    @Test
    fun `initial transition starts loading the initial query`() {
        val transition = FeedReducer.initial(initialQuery)

        assertEquals(
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Loading(requestId = 1L),
            ),
            transition.state,
        )
        assertEquals(
            listOf(
                FeedAction.StartLoad(
                    requestId = 1L,
                    query = initialQuery,
                ),
            ),
            transition.actions,
        )
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `query change cancels active load and starts a new one`() {
        val state =
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Loading(requestId = 1L),
            )

        val transition = FeedReducer.reduceIntent(state, FeedIntent.QueryChanged(updatedQuery))

        assertEquals(
            FeedState(
                query = updatedQuery,
                loadState = FeedState.LoadState.Loading(requestId = 2L),
            ),
            transition.state,
        )
        assertEquals(
            listOf(
                FeedAction.CancelLoad(requestId = 1L),
                FeedAction.StartLoad(
                    requestId = 2L,
                    query = updatedQuery,
                ),
            ),
            transition.actions,
        )
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `same query change is ignored`() {
        val state =
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Loading(requestId = 1L),
            )

        val transition = FeedReducer.reduceIntent(state, FeedIntent.QueryChanged(initialQuery))

        assertEquals(state, transition.state)
        assertTrue(transition.actions.isEmpty())
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `refresh request starts a refresh operation`() {
        val state = FeedState(query = initialQuery)

        val transition = FeedReducer.reduceIntent(state, FeedIntent.RefreshRequested)

        assertEquals(
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Refreshing(requestId = 1L),
            ),
            transition.state,
        )
        assertEquals(
            listOf(
                FeedAction.StartRefresh(requestId = 1L),
            ),
            transition.actions,
        )
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `refresh request cancels current refresh before restarting`() {
        val state =
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Refreshing(requestId = 3L),
            )

        val transition = FeedReducer.reduceIntent(state, FeedIntent.RefreshRequested)

        assertEquals(
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Refreshing(requestId = 4L),
            ),
            transition.state,
        )
        assertEquals(
            listOf(
                FeedAction.CancelRefresh(requestId = 3L),
                FeedAction.StartRefresh(requestId = 4L),
            ),
            transition.actions,
        )
    }

    @Test
    fun `load failure is stored and exposed as a one-shot label`() {
        val state =
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Loading(requestId = 1L),
            )

        val transition = FeedReducer.reduceMsg(state, FeedMsg.LoadFailed(1L, "load failed"))

        assertEquals(
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Failed(requestId = 1L, message = "load failed"),
            ),
            transition.state,
        )
        assertEquals(
            listOf(FeedLabel.ShowMessage("load failed")),
            transition.labels,
        )
    }

    @Test
    fun `stale load failure is ignored after cancellation`() {
        val state =
            FeedState(
                query = updatedQuery,
                loadState = FeedState.LoadState.Loading(requestId = 2L),
            )

        val transition = FeedReducer.reduceMsg(state, FeedMsg.LoadFailed(1L, "stale"))

        assertEquals(state, transition.state)
        assertTrue(transition.actions.isEmpty())
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `retry after load failure starts a new load`() {
        val state =
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Failed(requestId = 1L, message = "load failed"),
            )

        val transition = FeedReducer.reduceIntent(state, FeedIntent.RetryRequested)

        assertEquals(
            FeedState(
                query = initialQuery,
                loadState = FeedState.LoadState.Loading(requestId = 2L),
            ),
            transition.state,
        )
        assertEquals(
            listOf(
                FeedAction.StartLoad(
                    requestId = 2L,
                    query = initialQuery,
                ),
            ),
            transition.actions,
        )
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `refresh failure is stored and exposed as a one-shot label`() {
        val state =
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Refreshing(requestId = 2L),
            )

        val transition = FeedReducer.reduceMsg(state, FeedMsg.RefreshFailed(2L, "refresh failed"))

        assertEquals(
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Failed(requestId = 2L, message = "refresh failed"),
            ),
            transition.state,
        )
        assertEquals(
            listOf(FeedLabel.ShowMessage("refresh failed")),
            transition.labels,
        )
    }

    @Test
    fun `stale refresh failure is ignored after cancellation`() {
        val state =
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Refreshing(requestId = 2L),
            )

        val transition = FeedReducer.reduceMsg(state, FeedMsg.RefreshFailed(1L, "stale"))

        assertEquals(state, transition.state)
        assertTrue(transition.actions.isEmpty())
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `retry after refresh failure starts a new refresh`() {
        val state =
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Failed(requestId = 1L, message = "refresh failed"),
            )

        val transition = FeedReducer.reduceIntent(state, FeedIntent.RetryRequested)

        assertEquals(
            FeedState(
                query = initialQuery,
                refreshState = FeedState.RefreshState.Refreshing(requestId = 2L),
            ),
            transition.state,
        )
        assertEquals(
            listOf(
                FeedAction.StartRefresh(requestId = 2L),
            ),
            transition.actions,
        )
        assertTrue(transition.labels.isEmpty())
    }

    @Test
    fun `article click emits navigation label`() {
        val state = FeedState(query = initialQuery)

        val transition =
            FeedReducer.reduceIntent(
                state,
                FeedIntent.ArticleClicked(ContentId("article-1")),
            )

        assertEquals(state, transition.state)
        assertTrue(transition.actions.isEmpty())
        assertEquals(
            listOf(FeedLabel.OpenArticle(ContentId("article-1"))),
            transition.labels,
        )
    }
}
