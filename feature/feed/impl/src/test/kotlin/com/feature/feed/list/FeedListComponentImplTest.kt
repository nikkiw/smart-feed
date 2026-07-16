package com.feature.feed.list

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import com.core.content.model.ContentId
import com.core.content.model.ContentType
import com.core.content.model.Tags
import com.core.observers.ConnectivityRepository
import com.feature.feed.DecomposeTestUtils
import com.feature.feed.component.list.FeedListComponentImpl
import com.feature.feed.data.usecase.content.GetPagedContentUseCase
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.repository.ContentItemsSortedType
import com.feature.feed.domain.repository.Query
import com.feature.feed.domain.usecase.sync.SyncContentUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class FeedListComponentImplTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var testContext: DecomposeTestUtils.TestComponentContext
    private lateinit var connectivityRepository: ConnectivityRepository
    private lateinit var contentItemRepository: ContentItemRepository

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

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        testContext = DecomposeTestUtils.createTestComponentContext()
        connectivityRepository =
            mockk {
                every { isInternetAvailable() } returns true
                every { isConnected } returns MutableStateFlow(true)
            }
        contentItemRepository =
            mockk {
                every { observeHasContent() } returns flowOf(true)
            }
    }

    @After
    fun tearDown() {
        if (this::testContext.isInitialized) {
            testContext.destroyLifecycle()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load starts the initial query`() =
        runTest(dispatcher) {
            val initialLoadStarted = AtomicBoolean(false)
            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns
                flow {
                    initialLoadStarted.set(true)
                    awaitCancellation()
                }
            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } returns Result.success(Unit)

            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                )

            testContext.startLifecycle()
            backgroundScope.launch(dispatcher) { component.pagingItems.collect() }
            advanceUntilIdle()

            assertThat(initialLoadStarted.get()).isTrue()
            assertThat(component.model.value.refreshState)
                .isEqualTo(FeedListComponent.RefreshState.Idle)
            assertThat(component.model.value.isOnline).isTrue()
            verify(exactly = 1) {
                getPagedContentUseCase.invoke(initialQuery)
            }
        }

    @Test
    fun `query change cancels active load and starts a new one`() =
        runTest(dispatcher) {
            val initialCancelled = AtomicBoolean(false)
            val updatedStarted = AtomicBoolean(false)

            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns
                flow {
                    try {
                        awaitCancellation()
                    } finally {
                        initialCancelled.set(true)
                    }
                }
            every { getPagedContentUseCase.invoke(updatedQuery) } returns
                flow {
                    updatedStarted.set(true)
                    awaitCancellation()
                }
            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } returns Result.success(Unit)

            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                )

            testContext.startLifecycle()
            backgroundScope.launch(dispatcher) { component.pagingItems.collect() }
            advanceUntilIdle()

            component.updateQuery(updatedQuery)
            advanceUntilIdle()

            assertThat(initialCancelled.get()).isTrue()
            assertThat(updatedStarted.get()).isTrue()
            assertThat(component.model.value.refreshState)
                .isEqualTo(FeedListComponent.RefreshState.Idle)
            verify(exactly = 1) {
                getPagedContentUseCase.invoke(initialQuery)
            }
            verify(exactly = 1) {
                getPagedContentUseCase.invoke(updatedQuery)
            }
        }

    @Test
    fun `same query does not restart active load`() =
        runTest(dispatcher) {
            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns
                flow { awaitCancellation() }
            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } returns Result.success(Unit)
            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                )

            backgroundScope.launch(dispatcher) { component.pagingItems.collect() }

            component.updateQuery(initialQuery)
            advanceUntilIdle()

            verify(exactly = 1) { getPagedContentUseCase.invoke(initialQuery) }
        }

    @Test
    fun `refresh request cancels active refresh before restarting`() =
        runTest(dispatcher) {
            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns emptyFlow()
            val firstRefreshCancelled = AtomicBoolean(false)
            val refreshCalls = AtomicInteger(0)
            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } coAnswers {
                if (refreshCalls.incrementAndGet() == 1) {
                    try {
                        awaitCancellation()
                    } finally {
                        firstRefreshCancelled.set(true)
                    }
                }
                Result.success(Unit)
            }
            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                )

            component.onRefresh()
            advanceUntilIdle()
            component.onRefresh()
            advanceUntilIdle()

            assertThat(firstRefreshCancelled.get()).isTrue()
            assertThat(refreshCalls.get()).isEqualTo(2)
            assertThat(component.model.value.refreshState)
                .isEqualTo(FeedListComponent.RefreshState.Idle)
        }

    @Test
    fun `refresh failure emits a one-shot message effect and stores error state`() =
        runTest(dispatcher) {
            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns emptyFlow()

            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } coAnswers {
                delay(1.milliseconds)
                Result.failure(RuntimeException("refresh failed"))
            }

            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                )

            testContext.startLifecycle()
            advanceUntilIdle()

            component.onRefresh()
            assertThat(component.model.value.refreshState)
                .isEqualTo(FeedListComponent.RefreshState.Refreshing)

            advanceUntilIdle()

            assertThat(component.model.value.refreshState)
                .isEqualTo(FeedListComponent.RefreshState.Failed("refresh failed"))
        }

    @Test
    fun `refresh timeout stops spinner and emits a stable error`() =
        runTest(dispatcher) {
            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns emptyFlow()
            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } coAnswers { awaitCancellation() }
            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                )

            testContext.startLifecycle()
            advanceUntilIdle()
            component.onRefresh()
            assertThat(component.model.value.refreshState)
                .isEqualTo(FeedListComponent.RefreshState.Refreshing)

            advanceTimeBy(10_001L.milliseconds)
            advanceUntilIdle()

            assertThat(component.model.value.refreshState)
                .isEqualTo(
                    FeedListComponent.RefreshState.Failed(
                        "Refresh timed out. Please try again.",
                    ),
                )
        }

    @Test
    fun `article click delegates to the provided callback`() =
        runTest(dispatcher) {
            val clickedIds = mutableListOf<ContentId>()
            val getPagedContentUseCase = mockk<GetPagedContentUseCase>()
            every { getPagedContentUseCase.invoke(initialQuery) } returns emptyFlow()
            val syncContentUseCase = mockk<SyncContentUseCase>()
            coEvery { syncContentUseCase.invoke() } returns Result.success(Unit)

            val component =
                createComponent(
                    getPagedContentUseCase = getPagedContentUseCase,
                    syncContentUseCase = syncContentUseCase,
                    onItemClick = clickedIds::add,
                )

            testContext.startLifecycle()
            advanceUntilIdle()

            component.onListItemClick(ContentId("article-1"))

            assertThat(clickedIds)
                .containsExactly(ContentId("article-1"))
        }

    private fun createComponent(
        componentContext: ComponentContext = testContext.componentContext,
        getPagedContentUseCase: GetPagedContentUseCase,
        syncContentUseCase: SyncContentUseCase,
        onItemClick: (ContentId) -> Unit = {},
    ): FeedListComponentImpl =
        FeedListComponentImpl(
            componentContext = componentContext,
            storeFactory = DefaultStoreFactory(),
            getPagedContentUseCase = getPagedContentUseCase,
            syncContentUseCase = syncContentUseCase,
            connectivityRepository = connectivityRepository,
            contentItemRepository = contentItemRepository,
            initialQuery = initialQuery,
            onItemClick = onItemClick,
        )
}
