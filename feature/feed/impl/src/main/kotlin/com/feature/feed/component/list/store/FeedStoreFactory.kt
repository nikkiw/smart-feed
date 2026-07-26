package com.feature.feed.component.list.store

import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import com.core.observers.ConnectivityRepository
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.repository.Query
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class FeedStoreFactory(
    private val storeFactory: StoreFactory,
    private val initialQuery: Query,
    private val syncContentUseCase: SyncContentUseCase,
    private val connectivityRepository: ConnectivityRepository,
    private val contentItemRepository: ContentItemRepository,
) {
    fun create(): FeedStore = object :
        FeedStore,
        Store<FeedIntent, FeedState, FeedLabel> by storeFactory.create(
            name = "FeedStore",
            initialState =
            FeedState(
                query = initialQuery,
                isOnline = connectivityRepository.isConnected.value,
            ),
            bootstrapper = SimpleBootstrapper(FeedAction.Bootstrap),
            executorFactory = ::ExecutorImpl,
            reducer = FeedReducer,
        ) {}

    private inner class ExecutorImpl :
        CoroutineExecutor<FeedIntent, FeedAction, FeedState, FeedMsg, FeedLabel>() {
        private var nextRequestId = 0L
        private var refreshJob: Job? = null
        private var retryOnReconnect = false
        private var wasOnline = connectivityRepository.isConnected.value

        override fun executeAction(action: FeedAction) {
            when (action) {
                FeedAction.Bootstrap -> {
                    scope.launch {
                        connectivityRepository.isConnected.collect(::onConnectivityChanged)
                    }
                    scope.launch {
                        contentItemRepository.observeHasContent().collect {
                            dispatch(FeedMsg.LocalAvailabilityChanged(it))
                        }
                    }
                }
            }
        }

        override fun executeIntent(intent: FeedIntent) {
            when (intent) {
                is FeedIntent.QueryChanged -> {
                    if (intent.query != state().query) {
                        dispatch(FeedMsg.QueryChanged(intent.query))
                    }
                }

                FeedIntent.RefreshRequested -> startRefresh()
                FeedIntent.RetryRequested -> startRefresh()
                FeedIntent.EnableInternetClicked -> {
                    retryOnReconnect = true
                    publish(FeedLabel.OpenInternetSettings)
                }

                is FeedIntent.ArticleClicked -> publish(FeedLabel.OpenArticle(intent.preview))
            }
        }

        private fun onConnectivityChanged(isOnline: Boolean) {
            dispatch(FeedMsg.ConnectivityChanged(isOnline))
            if (!wasOnline && isOnline && retryOnReconnect) {
                retryOnReconnect = false
                startRefresh()
            }
            wasOnline = isOnline
        }

        private fun startRefresh() {
            refreshJob?.cancel()
            val requestId = ++nextRequestId
            dispatch(FeedMsg.RefreshStarted(requestId))
            refreshJob =
                scope.launch {
                    /*
                     * Cause: The refresh process uses withTimeout(10_000). If the request exceeds this duration,
                     * it throws a TimeoutCancellationException.
                     *
                     * Since runSuspendCatching (or a strict runCatching implementation) rethrows CancellationException
                     * to maintain Structured Concurrency, and TimeoutCancellationException is a subclass of
                     * CancellationException, the exception was bypassing the .fold block.
                     * This resulted in the coroutine crashing and the state getting stuck in "Refreshing".
                     */
                    val result =
                        //
                        runCatching {
                            withTimeout(REFRESH_TIMEOUT_MILLIS.milliseconds) { syncContentUseCase() }
                        }.fold(
                            onSuccess = { it },
                            onFailure = { throwable ->
                                if (throwable is CancellationException &&
                                    throwable !is TimeoutCancellationException
                                ) {
                                    throw throwable
                                }
                                Result.failure(throwable)
                            },
                        )

                    result.fold(
                        onSuccess = { dispatch(FeedMsg.RefreshCompleted(requestId)) },
                        onFailure = { throwable ->
                            if (throwable is CancellationException &&
                                throwable !is TimeoutCancellationException
                            ) {
                                throw throwable
                            }
                            val message =
                                if (throwable is TimeoutCancellationException) {
                                    REFRESH_TIMEOUT_ERROR
                                } else {
                                    throwable.message ?: UNKNOWN_REFRESH_ERROR
                                }
                            dispatch(FeedMsg.RefreshFailed(requestId, message))
                        },
                    )
                }
        }
    }

    private companion object {
        const val UNKNOWN_REFRESH_ERROR = "Failed refresh data (unknown error)"
        const val REFRESH_TIMEOUT_ERROR = "Refresh timed out. Please try again."
        const val REFRESH_TIMEOUT_MILLIS = 10_000L
    }
}
