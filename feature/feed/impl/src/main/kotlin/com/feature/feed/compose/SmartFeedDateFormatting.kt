package com.feature.feed.compose

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.core.content.model.UpdatedAt

@Composable
internal fun rememberFormattedUpdatedAt(updatedAt: UpdatedAt): String {
    val context = LocalContext.current
    return remember(updatedAt, context) {
        DateFormat.getMediumDateFormat(context).format(updatedAt.toDate()) +
            " " +
            DateFormat.getTimeFormat(context).format(updatedAt.toDate())
    }
}
