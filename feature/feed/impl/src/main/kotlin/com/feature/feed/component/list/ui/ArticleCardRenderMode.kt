package com.feature.feed.component.list.ui

/**
 * UI-only renderer choice used to compare XML and Compose cards in the same APK.
 */
enum class ArticleCardRenderMode(
    val wireValue: String,
) {
    Xml("xml"),
    Compose("compose"),
    ;

    companion object {
        const val EXTRA_ARTICLE_CARD_RENDERER =
            "com.ndev.android.smart.feed.extra.ARTICLE_CARD_RENDERER"

        fun fromWireValue(value: String?): ArticleCardRenderMode = entries.firstOrNull { it.wireValue == value } ?: Xml
    }
}
