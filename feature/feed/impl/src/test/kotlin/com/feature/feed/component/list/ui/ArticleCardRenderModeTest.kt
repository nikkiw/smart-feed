package com.feature.feed.component.list.ui

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ArticleCardRenderModeTest {
    @Test
    fun `compose wire value selects compose renderer`() {
        assertThat(ArticleCardRenderMode.fromWireValue("compose"))
            .isEqualTo(ArticleCardRenderMode.Compose)
    }

    @Test
    fun `missing or unknown renderer falls back to xml`() {
        assertThat(ArticleCardRenderMode.fromWireValue(null))
            .isEqualTo(ArticleCardRenderMode.Xml)
        assertThat(ArticleCardRenderMode.fromWireValue("unexpected"))
            .isEqualTo(ArticleCardRenderMode.Xml)
    }
}
