package com.feature.feed.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun TagChip(tag: String, modifier: Modifier = Modifier) {
    val spacing = SmartFeedThemeTokens.spacing

    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = CircleShape,
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
internal fun LoadingState(title: String, message: String, modifier: Modifier = Modifier) {
    val spacing = SmartFeedThemeTokens.spacing

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.medium),
            modifier = Modifier.padding(spacing.xLarge),
        ) {
            CircularProgressIndicator()
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun EmptyState(
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val spacing = SmartFeedThemeTokens.spacing

    Box(
        modifier =
        modifier
            .fillMaxSize()
            .padding(spacing.xLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 360.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.large),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (actionText != null && onAction != null) {
                OutlinedButton(onClick = onAction) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
internal fun InlineErrorCard(
    message: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = SmartFeedThemeTokens.spacing

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(spacing.large),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Spacer(modifier = Modifier.width(spacing.medium))
            AssistChip(
                onClick = onAction,
                label = { Text(actionText) },
                leadingIcon = {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingStatePreview() {
    SmartFeedTheme {
        LoadingState(
            title = "Preparing feed",
            message = "Loading local content and checking for updates",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyStatePreview() {
    SmartFeedTheme {
        EmptyState(
            message = "No articles match the selected filters.",
            actionText = "Retry",
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun InlineErrorCardPreview() {
    SmartFeedTheme {
        InlineErrorCard(
            message = "Unable to load the next page.",
            actionText = "Retry",
            onAction = {},
        )
    }
}
