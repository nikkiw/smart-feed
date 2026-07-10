package com.feature.feed.store

object FeedReducer {
    fun initial(query: com.feature.feed.domain.repository.Query): FeedTransition {
        val requestId = 1L
        return FeedTransition(
            state =
                FeedState(
                    query = query,
                    loadState = FeedState.LoadState.Loading(requestId = requestId),
                ),
            actions =
                listOf(
                    FeedAction.StartLoad(
                        requestId = requestId,
                        query = query,
                    ),
                ),
        )
    }

    fun reduceIntent(
        state: FeedState,
        intent: FeedIntent,
    ): FeedTransition =
        when (intent) {
            is FeedIntent.QueryChanged -> onQueryChanged(state, intent.query)
            FeedIntent.RefreshRequested -> onRefreshRequested(state)
            FeedIntent.RetryRequested -> onRetryRequested(state)
            is FeedIntent.ArticleClicked ->
                FeedTransition(
                    state = state,
                    labels = listOf(FeedLabel.OpenArticle(intent.contentId)),
                )
        }

    fun reduceMsg(
        state: FeedState,
        msg: FeedMsg,
    ): FeedTransition =
        when (msg) {
            is FeedMsg.LoadCompleted -> onLoadCompleted(state, msg.requestId)
            is FeedMsg.LoadFailed -> onLoadFailed(state, msg.requestId, msg.message)
            is FeedMsg.RefreshCompleted -> onRefreshCompleted(state, msg.requestId)
            is FeedMsg.RefreshFailed -> onRefreshFailed(state, msg.requestId, msg.message)
        }

    private fun onQueryChanged(
        state: FeedState,
        query: com.feature.feed.domain.repository.Query,
    ): FeedTransition {
        if (state.query == query) {
            return FeedTransition(state = state)
        }

        val nextRequestId = nextLoadRequestId(state)
        val actions =
            buildList {
                if (state.loadState is FeedState.LoadState.Loading) {
                    add(FeedAction.CancelLoad(state.loadState.requestId))
                }
                add(
                    FeedAction.StartLoad(
                        requestId = nextRequestId,
                        query = query,
                    ),
                )
            }

        return FeedTransition(
            state =
                state.copy(
                    query = query,
                    loadState = FeedState.LoadState.Loading(requestId = nextRequestId),
                ),
            actions = actions,
        )
    }

    private fun onRefreshRequested(state: FeedState): FeedTransition {
        val nextRequestId = nextRefreshRequestId(state)
        val actions =
            buildList {
                if (state.refreshState is FeedState.RefreshState.Refreshing) {
                    add(FeedAction.CancelRefresh(state.refreshState.requestId))
                }
                add(FeedAction.StartRefresh(requestId = nextRequestId))
            }

        return FeedTransition(
            state =
                state.copy(
                    refreshState = FeedState.RefreshState.Refreshing(requestId = nextRequestId),
                ),
            actions = actions,
        )
    }

    private fun onRetryRequested(state: FeedState): FeedTransition =
        when (val failure = state.loadState) {
            is FeedState.LoadState.Failed ->
                startLoad(
                    state = state,
                    query = state.query,
                    clearRefreshState = false,
                    previousRequestId = failure.requestId,
                )

            else ->
                when (val refreshFailure = state.refreshState) {
                    is FeedState.RefreshState.Failed ->
                        startRefresh(
                            state = state,
                            previousRequestId = refreshFailure.requestId,
                        )

                    else -> FeedTransition(state = state)
                }
        }

    private fun onLoadCompleted(
        state: FeedState,
        requestId: Long,
    ): FeedTransition {
        val activeLoadState = state.loadState
        if (activeLoadState !is FeedState.LoadState.Loading || activeLoadState.requestId != requestId) {
            return FeedTransition(state = state)
        }

        return FeedTransition(
            state =
                state.copy(
                    loadState = FeedState.LoadState.Idle,
                ),
        )
    }

    private fun onLoadFailed(
        state: FeedState,
        requestId: Long,
        message: String,
    ): FeedTransition {
        val activeLoadState = state.loadState
        if (activeLoadState !is FeedState.LoadState.Loading || activeLoadState.requestId != requestId) {
            return FeedTransition(state = state)
        }

        return FeedTransition(
            state =
                state.copy(
                    loadState = FeedState.LoadState.Failed(requestId = requestId, message = message),
                ),
            labels = listOf(FeedLabel.ShowMessage(message)),
        )
    }

    private fun onRefreshCompleted(
        state: FeedState,
        requestId: Long,
    ): FeedTransition {
        val activeRefreshState = state.refreshState
        if (activeRefreshState !is FeedState.RefreshState.Refreshing || activeRefreshState.requestId != requestId) {
            return FeedTransition(state = state)
        }

        return FeedTransition(
            state =
                state.copy(
                    refreshState = FeedState.RefreshState.Idle,
                ),
        )
    }

    private fun onRefreshFailed(
        state: FeedState,
        requestId: Long,
        message: String,
    ): FeedTransition {
        val activeRefreshState = state.refreshState
        if (activeRefreshState !is FeedState.RefreshState.Refreshing || activeRefreshState.requestId != requestId) {
            return FeedTransition(state = state)
        }

        return FeedTransition(
            state =
                state.copy(
                    refreshState = FeedState.RefreshState.Failed(requestId = requestId, message = message),
                ),
            labels = listOf(FeedLabel.ShowMessage(message)),
        )
    }

    private fun startLoad(
        state: FeedState,
        query: com.feature.feed.domain.repository.Query,
        clearRefreshState: Boolean,
        previousRequestId: Long,
    ): FeedTransition {
        val nextRequestId = maxOf(previousRequestId, stateAsLoadRequestId(state)) + 1L
        val actions =
            listOf(
                FeedAction.StartLoad(
                    requestId = nextRequestId,
                    query = query,
                ),
            )

        return FeedTransition(
            state =
                state.copy(
                    query = query,
                    loadState = FeedState.LoadState.Loading(requestId = nextRequestId),
                    refreshState = if (clearRefreshState) FeedState.RefreshState.Idle else state.refreshState,
                ),
            actions = actions,
        )
    }

    private fun startRefresh(
        state: FeedState,
        previousRequestId: Long,
    ): FeedTransition {
        val nextRequestId = maxOf(previousRequestId, stateAsRefreshRequestId(state)) + 1L

        return FeedTransition(
            state =
                state.copy(
                    refreshState = FeedState.RefreshState.Refreshing(requestId = nextRequestId),
                ),
            actions =
                listOf(
                    FeedAction.StartRefresh(requestId = nextRequestId),
                ),
        )
    }

    private fun nextLoadRequestId(state: FeedState): Long = stateAsLoadRequestId(state) + 1L

    private fun nextRefreshRequestId(state: FeedState): Long = stateAsRefreshRequestId(state) + 1L

    private fun stateAsLoadRequestId(state: FeedState): Long =
        when (val loadState = state.loadState) {
            is FeedState.LoadState.Loading -> loadState.requestId
            is FeedState.LoadState.Failed -> loadState.requestId
            FeedState.LoadState.Idle -> 0L
        }

    private fun stateAsRefreshRequestId(state: FeedState): Long =
        when (val refreshState = state.refreshState) {
            is FeedState.RefreshState.Refreshing -> refreshState.requestId
            is FeedState.RefreshState.Failed -> refreshState.requestId
            FeedState.RefreshState.Idle -> 0L
        }
}
