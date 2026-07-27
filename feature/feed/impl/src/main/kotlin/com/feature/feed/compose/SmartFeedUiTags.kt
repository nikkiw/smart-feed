package com.feature.feed.compose

/**
 * Stable UI anchors shared by Compose tests and macrobenchmark / baseline-profile flows.
 *
 * When [androidx.compose.ui.semantics.testTagsAsResourceId] is enabled at the root,
 * these tags become addressable from UiAutomator via `By.res(tag)`.
 */
internal object SmartFeedUiTags {
    const val ROOT = "smartfeed_root"
    const val FEED_SCREEN = "feed_screen"
    const val FEED_LIST = "feed_list"
    const val PREVIEW_CARD = "preview_card"
    const val ARTICLE_SCREEN = "article_screen"
    const val ARTICLE_CONTENT = "article_content"
    const val RECOMMENDATION_SCREEN = "recommendation_screen"
    const val RECOMMENDATION_LIST = "recommendation_list"
    const val TAB_FEED = "tab_feed"
    const val TAB_RECOMMENDATIONS = "tab_recommendations"
}
