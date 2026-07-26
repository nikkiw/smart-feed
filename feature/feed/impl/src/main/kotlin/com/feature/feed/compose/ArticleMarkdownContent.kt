package com.feature.feed.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState

@Composable
internal fun ArticleMarkdownContent(content: String, modifier: Modifier = Modifier) {
    val state =
        rememberMarkdownState(
            content.trim(),
            retainState = true,
        )

    val typography =
        markdownTypography(
            h1 = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            h2 = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            h3 = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            h4 = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            h5 = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            h6 = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
        )

    Markdown(
        state,
        typography = typography,
        modifier = modifier,
        imageTransformer = Coil3ImageTransformerImpl,
    )
}
