package com.core.domain

import io.mockk.*
import androidx.paging.PagingData
import com.core.domain.model.Content
import com.core.domain.model.ContentItem
import com.core.domain.model.ContentItemId
import com.core.domain.model.ContentItemType
import com.core.domain.model.ImageUrl
import com.core.domain.model.Tags
import com.core.domain.model.Title
import com.core.domain.model.UpdatedAt
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import com.core.domain.usecase.content.GetContentUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals


@ExperimentalCoroutinesApi
class GetContentUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<ContentItemRepository>()
    private lateinit var useCase: GetContentUseCase

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        useCase = GetContentUseCase(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `invoke returns flow from repository`() = runTest {
        // given
        val query = Query(
            types = listOf(ContentItemType.ARTICLE),
            tags = Tags(),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )
        val expected = flowOf(
            PagingData.from(
                listOf<ContentItem>(
                    ContentItem.Article(
                        id = ContentItemId("1"),
                        updatedAt = UpdatedAt.parse("2023-01-01T10:00:00Z"),
                        mainImageUrl = ImageUrl("https://example.com/img.jpg"),
                        tags = Tags(listOf("tag1", "tag2")),
                        title = Title("Example Article"),
                        content = Content("Content ccccxcdsf af dsf adfds fadf adsf fasf af")
                    )
                )
            )
        )
        every { repository.flowContent(query) } returns expected

        // when
        val result = useCase(query)

        // then
        assertEquals(expected, result)
        verify(exactly = 1) { repository.flowContent(query) }
    }
}
