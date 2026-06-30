package com.feature.feed.master

import androidx.paging.PagingData
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.core.content.model.ContentId
import com.feature.feed.domain.model.ContentItemPreview
import com.core.content.model.ContentType
import com.core.content.model.Tags
import com.feature.feed.domain.repository.ContentItemRepository
import com.feature.feed.domain.repository.ContentItemsSortedType
import com.feature.feed.domain.repository.Query
import com.feature.feed.DecomposeTestUtils
import com.feature.feed.list.FeedListComponent
import com.google.common.truth.Truth.assertThat
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FeedMasterComponentImplTest {
    private val dispatcher = UnconfinedTestDispatcher()
    private lateinit var testContext: DecomposeTestUtils.TestComponentContext

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        testContext = DecomposeTestUtils.createTestComponentContext()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filter and sort changes are delegated to feed list contract`() =
        runTest(dispatcher) {
            val feedList = RecordingFeedListComponent()
            val factory =
                FeedListComponent.Factory { _, initialQuery, _ ->
                    feedList.initialQuery = initialQuery
                    feedList
                }
            val component =
                FeedMasterComponentImpl(
                    componentContext = testContext.componentContext,
                    contentItemRepository = mockk<ContentItemRepository>(relaxed = true),
                    feedListComponentFactory = factory,
                    onListItemClick = {},
                )

            testContext.startLifecycle()

            assertThat(feedList.initialQuery)
                .isEqualTo(
                    Query(
                        types = listOf(ContentType.ARTICLE),
                        tags = Tags(),
                        sortedBy = ContentItemsSortedType.ByDateNewestFirst,
                    ),
                )

            component.onSortTypeSelected(ContentItemsSortedType.ByNameAsc)

            assertThat(feedList.queryUpdates.last())
                .isEqualTo(
                    Query(
                        types = listOf(ContentType.ARTICLE),
                        tags = Tags(),
                        sortedBy = ContentItemsSortedType.ByNameAsc,
                    ),
                )

            component.onTagsSelected(Tags(listOf("kotlin")))

            assertThat(feedList.queryUpdates.last())
                .isEqualTo(
                    Query(
                        types = listOf(ContentType.ARTICLE),
                        tags = Tags(listOf("kotlin")),
                        sortedBy = ContentItemsSortedType.ByNameAsc,
                    ),
                )
        }

    private class RecordingFeedListComponent : FeedListComponent {
        var initialQuery: Query? = null
        val queryUpdates = mutableListOf<Query>()

        override val pagingItems: Value<PagingData<ContentItemPreview>> =
            MutableValue(PagingData.empty())
        override val isRefreshing: Value<FeedListComponent.State> =
            MutableValue(FeedListComponent.State.RefreshSuccess)
        override val isOnline: Boolean = true

        override fun onRefresh() = Unit

        override fun onListItemClick(itemId: ContentId) = Unit

        override fun updateQuery(query: Query) {
            queryUpdates += query
        }
    }
}
