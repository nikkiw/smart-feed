package com.feature.feed.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.core.content.model.ContentId
import com.core.content.model.ImageUrl
import com.core.content.model.ShortDescription
import com.core.content.model.Tags
import com.core.content.model.Title
import com.core.content.model.UpdatedAt
import com.feature.feed.domain.model.ContentItemPreview

@Composable
internal fun PreviewCard(preview: ContentItemPreview, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val spacing = SmartFeedThemeTokens.spacing
    val formattedUpdatedAt = rememberFormattedUpdatedAt(preview.updatedAt)
    val sharedTransitionContentId = LocalSharedTransitionContentId.current

    Card(
        modifier =
        modifier
            .smartFeedSharedBounds(
                sharedCardKey(preview.id.value),
                preview.id.value,
                clipShape = MaterialTheme.shapes.large,
            )
            .fillMaxWidth()
            .testTag(SmartFeedUiTags.PREVIEW_CARD)
            .clickable {
                sharedTransitionContentId?.value = preview.id.value
                onClick()
            },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = spacing.xSmall),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.medium)) {
            AsyncImage(
                model = preview.mainImageUrl.value.ifBlank { null },
                contentDescription = null,
                modifier =
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .smartFeedSharedBounds(
                        sharedImageKey(preview.id.value),
                        preview.id.value,
                        clipShape = MaterialTheme.shapes.large,
                    ),
                contentScale = ContentScale.Crop,
            )

            Column(
                modifier = Modifier.padding(start = spacing.large, end = spacing.large, bottom = spacing.large),
                verticalArrangement = Arrangement.spacedBy(spacing.small),
            ) {
                Text(
                    text =
                    when (preview) {
                        is ContentItemPreview.ArticlePreview -> preview.title.value
                        is ContentItemPreview.UnknownPreview -> preview.rawType
                    },
                    modifier =
                    Modifier
                        .smartFeedSharedBounds(sharedTitleKey(preview.id.value), preview.id.value)
                        .semantics { heading() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (preview is ContentItemPreview.ArticlePreview) {
                    Text(
                        text = preview.short.value,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = formattedUpdatedAt,
                    modifier =
                    Modifier.smartFeedSharedBounds(
                        sharedUpdatedAtKey(preview.id.value),
                        preview.id.value,
                    ),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (preview.tags.value.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(spacing.small),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items(
                            items = preview.tags.value,
                            key = { it },
                        ) { tag ->
                            TagChip(
                                tag = tag,
                                modifier =
                                Modifier.smartFeedSharedBounds(
                                    sharedTagKey(preview.id.value, tag),
                                    preview.id.value,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCardPreview() {
    SmartFeedTheme {
        PreviewCard(
            preview =
            ContentItemPreview.ArticlePreview(
                id = ContentId("preview-id"),
                updatedAt = UpdatedAt.now(),
                mainImageUrl = ImageUrl(""),
                tags = Tags(listOf("Compose", "Architecture")),
                title = Title("Compose-only feed card"),
                short = ShortDescription("Article card preview with localized date and semantic heading."),
            ),
            onClick = {},
        )
    }
}
