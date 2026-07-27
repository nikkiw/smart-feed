package com.feature.feed.compose

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
internal data class SmartFeedSpacing(
    val xSmall: androidx.compose.ui.unit.Dp = 4.dp,
    val small: androidx.compose.ui.unit.Dp = 8.dp,
    val medium: androidx.compose.ui.unit.Dp = 12.dp,
    val large: androidx.compose.ui.unit.Dp = 16.dp,
    val xLarge: androidx.compose.ui.unit.Dp = 24.dp,
)

internal val LocalSmartFeedSpacing = staticCompositionLocalOf { SmartFeedSpacing() }

internal object SmartFeedThemeTokens {
    val spacing: SmartFeedSpacing
        @Composable get() = LocalSmartFeedSpacing.current
}

internal val SmartFeedTopBarHeight = 64.dp
internal val SmartFeedBottomBarHeight = 80.dp

private val SmartFeedLightColors =
    lightColorScheme(
        primary = Color(0xFF2563EB),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFDCEBFF),
        onPrimaryContainer = Color(0xFF001C3A),
        secondary = Color(0xFF475569),
        secondaryContainer = Color(0xFFE2E8F0),
        onSecondaryContainer = Color(0xFF0F172A),
        surface = Color(0xFFF8FAFC),
        background = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFFE2E8F0),
        outline = Color(0xFF94A3B8),
        error = Color(0xFFB42318),
    )

private val SmartFeedDarkColors =
    darkColorScheme(
        primary = Color(0xFF93C5FD),
        onPrimary = Color(0xFF0B1F3A),
        primaryContainer = Color(0xFF163B70),
        onPrimaryContainer = Color(0xFFDCEBFF),
        secondary = Color(0xFFCBD5E1),
        secondaryContainer = Color(0xFF334155),
        onSecondaryContainer = Color(0xFFE2E8F0),
        surface = Color(0xFF020617),
        background = Color(0xFF020617),
        surfaceVariant = Color(0xFF1E293B),
        outline = Color(0xFF64748B),
        error = Color(0xFFFDA29B),
    )

private val SmartFeedTypography =
    Typography(
        headlineMedium = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontSize = 24.sp, lineHeight = 30.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.SemiBold),
        titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
        labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    )

private val SmartFeedShapes =
    Shapes(
        small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
        large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
    )

@Composable
fun SmartFeedTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val darkTheme = isSystemInDarkTheme()
    val colorScheme =
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkTheme -> dynamicDarkColorScheme(context)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> dynamicLightColorScheme(context)
            darkTheme -> SmartFeedDarkColors
            else -> SmartFeedLightColors
        }

    CompositionLocalProvider(LocalSmartFeedSpacing provides SmartFeedSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = SmartFeedTypography,
            shapes = SmartFeedShapes,
            content = content,
        )
    }
}
