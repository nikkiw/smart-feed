@file:Suppress("ktlint:standard:function-naming")

package com.feature.feed.component.list.ui.compose

import android.content.Context
import androidx.annotation.AttrRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import com.google.android.material.R as MaterialR

/**
 * Bridges the host Material Components XML theme into the Compose island.
 */
@Composable
internal fun SmartFeedMaterialTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val colors = remember(context, isDark) { context.materialColors(isDark) }

    MaterialTheme(
        colors = colors,
        content = content,
    )
}

private fun Context.materialColors(isDark: Boolean): Colors {
    val defaults = if (isDark) darkColors() else lightColors()
    return defaults.copy(
        primary = themeColor(MaterialR.attr.colorPrimary, defaults.primary),
        primaryVariant = themeColor(MaterialR.attr.colorPrimaryVariant, defaults.primaryVariant),
        secondary = themeColor(MaterialR.attr.colorSecondary, defaults.secondary),
        secondaryVariant = themeColor(MaterialR.attr.colorSecondaryVariant, defaults.secondaryVariant),
        background = themeColor(android.R.attr.colorBackground, defaults.background),
        surface = themeColor(MaterialR.attr.colorSurface, defaults.surface),
        error = themeColor(MaterialR.attr.colorError, defaults.error),
        onPrimary = themeColor(MaterialR.attr.colorOnPrimary, defaults.onPrimary),
        onSecondary = themeColor(MaterialR.attr.colorOnSecondary, defaults.onSecondary),
        onBackground = themeColor(MaterialR.attr.colorOnBackground, defaults.onBackground),
        onSurface = themeColor(MaterialR.attr.colorOnSurface, defaults.onSurface),
        onError = themeColor(MaterialR.attr.colorOnError, defaults.onError),
    )
}

private fun Context.themeColor(
    @AttrRes attribute: Int,
    fallback: Color,
): Color = Color(MaterialColors.getColor(this, attribute, fallback.toArgb()))
