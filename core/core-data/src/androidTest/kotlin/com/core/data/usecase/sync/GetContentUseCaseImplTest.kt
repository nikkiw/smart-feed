package com.core.data.usecase.sync

import android.content.Context
import androidx.paging.testing.asSnapshot
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.core.data.repository.ContentItemRepositoryImpl
import com.core.data.usecase.content.GetContentUseCaseImpl
import com.core.database.AppDatabase
import com.core.database.content.entity.ArticleAttributesEntity
import com.core.database.content.entity.ContentEntity
import com.core.domain.model.ContentType
import com.core.domain.model.Tags
import com.core.domain.repository.ContentItemRepository
import com.core.domain.repository.ContentItemsSortedType
import com.core.domain.repository.Query
import com.core.domain.usecase.content.GetContentUseCase
import com.core.networks.datasource.dev.DevStaticJsonTestNetworkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(AndroidJUnit4::class)
class GetContentUseCaseImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var db: AppDatabase
    private lateinit var repository: ContentItemRepository
    private lateinit var useCase: GetContentUseCase

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = AppDatabase.getTestDatabase(context)

        repository = ContentItemRepositoryImpl(
            contentDao = db.contentDao(),
            contentTagsDao = db.contentTagsDao(),
            updatesMetaDao = db.updatesMetaDao(),
            networkDataSource = DevStaticJsonTestNetworkDataSource(context),
            ioDispatcher = testDispatcher
        )
        useCase = GetContentUseCaseImpl(repository)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        db.close()
    }

    // Гибкая вставка контента с заданными параметрами
    private suspend fun insertContent(
        id: String,
        type: String = "article",
        updatedAt: Long = System.currentTimeMillis(),
        tags: List<String> = listOf("tag1", "tag2")
    ) {
        val contentEntity = ContentEntity(
            id = id,
            type = type,
            action = "read",
            updatedAt = updatedAt,
            mainImageUrl = "https://example.com/image_$id.jpg",
            tags = tags
        )
        val articleAttributes = ArticleAttributesEntity(
            contentId = contentEntity.id,
            title = "Title ${contentEntity.id}",
            content = "Content for ${contentEntity.id}",
            shortDescription = "Short description for ${contentEntity.id}",
            unitEmbedding = FloatArray(10) { Random.nextDouble(0.1, 1.0).toFloat() }
        )
        db.contentDao().insertContentUpdateWithDetails(contentEntity, articleAttributes)
    }

    @Test
    fun invoke_emptyQuery_returnsAllContent() = runTest {
        // Preparation: insertion of 3 elements of different types
        insertContent("id1", type = "article", tags = listOf("tag1"))
        insertContent("id2", type = "video", tags = listOf("tag2"))
        insertContent("id3", type = "article", tags = listOf("tag1", "tag2"))

        // Request without filtering
        val query = Query(
            types = emptyList(),
            tags = Tags(emptyList()),
            sortedBy = ContentItemsSortedType.ByDateOldestFirst
        )

        // Check: all items must be returned
        val result = useCase(query).asSnapshot().map { it.id.value }
        assertEquals(setOf("id1", "id2", "id3"), result.toSet())
    }

    @Test
    fun invoke_noMatchingTags_returnsEmpty() = runTest {
        // Preparation: inserting elements with tags tag1 and tag2
        insertContent("id1", tags = listOf("tag1", "tag2"))

        // Request with inappropriate tag
        val query = Query(
            types = listOf(ContentType.ARTICLE), tags = Tags(listOf("tag3")),
            sortedBy = ContentItemsSortedType.ByDateOldestFirst
        )

        // Check: the result is empty
        val result = useCase(query).asSnapshot().map { it.id.value }
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_multipleTags_returnsOnlyMatching() = runTest {
        // Preparation: different combinations of tags
        insertContent("id1", tags = listOf("tag1"))
        insertContent("id2", tags = listOf("tag2"))
        insertContent("id3", tags = listOf("tag1", "tag2"))

        // Query with two tags
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(listOf("tag1")),
            sortedBy = ContentItemsSortedType.ByDateOldestFirst
        )

        // Check: only element with both tags
        val result = useCase(query).asSnapshot().map { it.id.value }
        assertEquals(listOf("id1", "id3"), result)
    }

    @Test
    fun invoke_sortByNewestFirst_returnsCorrectOrder() = runTest {
        // Preparation: insertion with different update times
        val timeOldest = 1000L
        val timeMiddle = 2000L
        val timeNewest = 3000L

        insertContent("id1", updatedAt = timeOldest)
        insertContent("id2", updatedAt = timeMiddle)
        insertContent("id3", updatedAt = timeNewest)

        // Query sorted by newness
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(emptyList()),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )

        // Check: items in descending date order
        val result = useCase(query).asSnapshot().map { it.id.value }
        assertEquals(listOf("id3", "id2", "id1"), result)
    }

    @Test
    fun invoke_noContent_returnsEmpty() = runTest {
        // Request without preliminary data insertion
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(emptyList()),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )

        // Check: the result is empty
        val result = useCase(query).asSnapshot().map { it.id.value }
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_multipleTypes_returnsAllMatching() = runTest {
        // Preparation: different types of content
        insertContent("id1", type = "article")
        insertContent("id2", type = "article")
        insertContent("id3", type = "article")

        // Query with multiple types
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(emptyList()),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )

        // Check: all elements are returned
        val result = useCase(query).asSnapshot().map { it.id.value }
        assertEquals(setOf("id1", "id2", "id3"), result.toSet())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun invoke_databaseUpdates_flowEmitsNewData() = runTest {
        val query = Query(
            types = listOf(ContentType.ARTICLE),
            tags = Tags(emptyList()),
            sortedBy = ContentItemsSortedType.ByDateNewestFirst
        )

        // First start of the stream
        val job = launch {
            useCase(query).collect {}
        }
        advanceUntilIdle()

        // Inserting the first element
        insertContent("id1")
        advanceUntilIdle()
        var result = useCase(query).asSnapshot().map { it.id.value }
        assertEquals(listOf("id1"), result)

        // Inserting the second element
        insertContent("id2")
        advanceUntilIdle()
        result = useCase(query).asSnapshot().map { it.id.value }
        assertEquals(listOf("id2", "id1"), result)

        job.cancel()
    }
}