package com.feature.feed.component.article

import com.feature.feed.article.ArticleItemComponent

private const val ARTICLE_READ_PROGRESS_BEFORE_BODY = 0.1f
private const val ARTICLE_READ_PROGRESS_AFTER_BODY = 0.9f
private const val RELATED_CONTENT_START_INDEX = 3

internal object ArticleReadProgressCalculator {
    fun calculate(snapshot: ArticleItemComponent.ReadProgressSnapshot): Float {
        return when {
            !snapshot.canScrollBackward -> 0f
            !snapshot.canScrollForward -> 1f
            snapshot.bodyItemOffset != null && snapshot.bodyItemSize != null -> {
                val bodyItemOffset = requireNotNull(snapshot.bodyItemOffset)
                val bodyItemSize = requireNotNull(snapshot.bodyItemSize)
                val viewportHeight =
                    (snapshot.viewportEndOffset - snapshot.viewportStartOffset).coerceAtLeast(1)
                val bodyScrollablePx = (bodyItemSize - viewportHeight).coerceAtLeast(0)
                val bodyProgress =
                    if (bodyScrollablePx == 0) {
                        val visiblePx =
                            (
                                minOf(
                                    bodyItemOffset + bodyItemSize,
                                    snapshot.viewportEndOffset,
                                ) - maxOf(bodyItemOffset, snapshot.viewportStartOffset)
                                ).coerceAtLeast(0)
                        if (bodyItemSize == 0) {
                            0f
                        } else {
                            visiblePx.toFloat() / bodyItemSize.toFloat()
                        }
                    } else {
                        (-bodyItemOffset).coerceIn(0, bodyScrollablePx).toFloat() /
                            bodyScrollablePx.toFloat()
                    }

                (
                    ARTICLE_READ_PROGRESS_BEFORE_BODY +
                        (ARTICLE_READ_PROGRESS_AFTER_BODY - ARTICLE_READ_PROGRESS_BEFORE_BODY) * bodyProgress
                    ).coerceIn(0f, ARTICLE_READ_PROGRESS_AFTER_BODY)
            }

            snapshot.firstVisibleItemIndex <= 0 -> 0f
            snapshot.firstVisibleItemIndex <= 1 -> ARTICLE_READ_PROGRESS_BEFORE_BODY
            snapshot.firstVisibleItemIndex <= 2 -> ARTICLE_READ_PROGRESS_AFTER_BODY
            else -> {
                val relatedItemsDenominator =
                    (snapshot.totalItemsCount - 1 - RELATED_CONTENT_START_INDEX).coerceAtLeast(1)
                val relatedProgress =
                    (
                        (snapshot.lastVisibleItemIndex - RELATED_CONTENT_START_INDEX).toFloat() /
                            relatedItemsDenominator.toFloat()
                        ).coerceIn(0f, 1f)

                (
                    ARTICLE_READ_PROGRESS_AFTER_BODY +
                        (1f - ARTICLE_READ_PROGRESS_AFTER_BODY) * relatedProgress
                    ).coerceIn(ARTICLE_READ_PROGRESS_AFTER_BODY, 1f)
            }
        }
    }
}
