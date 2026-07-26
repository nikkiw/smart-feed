package com.feature.feed.article

import com.feature.feed.article.ArticleItemComponent.ReadProgressSnapshot
import com.feature.feed.component.article.ArticleReadProgressCalculator
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ArticleReadProgressCalculatorTest {
    @Test
    fun `top of article reports zero progress`() {
        val progress =
            ArticleReadProgressCalculator.calculate(
                ReadProgressSnapshot(
                    firstVisibleItemIndex = 0,
                    lastVisibleItemIndex = 1,
                    totalItemsCount = 6,
                    canScrollBackward = false,
                    canScrollForward = true,
                    viewportStartOffset = 0,
                    viewportEndOffset = 1000,
                ),
            )

        assertThat(progress).isEqualTo(0f)
    }

    @Test
    fun `midway through article body stays within body progress range`() {
        val progress =
            ArticleReadProgressCalculator.calculate(
                ReadProgressSnapshot(
                    firstVisibleItemIndex = 1,
                    lastVisibleItemIndex = 2,
                    totalItemsCount = 6,
                    canScrollBackward = true,
                    canScrollForward = true,
                    viewportStartOffset = 0,
                    viewportEndOffset = 1000,
                    bodyItemOffset = -500,
                    bodyItemSize = 2000,
                ),
            )

        assertThat(progress).isGreaterThan(0.1f)
        assertThat(progress).isLessThan(0.9f)
    }

    @Test
    fun `bottom of article reports full progress`() {
        val progress =
            ArticleReadProgressCalculator.calculate(
                ReadProgressSnapshot(
                    firstVisibleItemIndex = 4,
                    lastVisibleItemIndex = 5,
                    totalItemsCount = 6,
                    canScrollBackward = true,
                    canScrollForward = false,
                    viewportStartOffset = 0,
                    viewportEndOffset = 1000,
                ),
            )

        assertThat(progress).isEqualTo(1f)
    }
}
