package com.smarttrip.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary            = Blue600,
    onPrimary          = SurfaceLight,
    primaryContainer   = Blue100,
    onPrimaryContainer = Blue900,
    secondary          = Violet500,
    onSecondary        = SurfaceLight,
    secondaryContainer = Violet100,
    onSecondaryContainer = Blue900,
    tertiary           = Emerald500,
    onTertiary         = SurfaceLight,
    background         = BackgroundLight,
    onBackground       = OnSurfaceLight,
    surface            = SurfaceLight,
    onSurface          = OnSurfaceLight,
    surfaceVariant     = Slate100,
    onSurfaceVariant   = OnSurfaceVariantLight,
    outline            = Slate200,
    outlineVariant     = Slate100,
    error              = Error500,
    errorContainer     = Rose100,
    onErrorContainer   = Color(0xFF7F1D1D),
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue400,
    onPrimary          = Blue900,
    primaryContainer   = Blue800,
    onPrimaryContainer = Blue100,
    secondary          = Violet500,
    onSecondary        = Blue900,
    secondaryContainer = Color(0xFF2E1065),
    onSecondaryContainer = Violet100,
    tertiary           = Emerald500,
    background         = BackgroundDark,
    onBackground       = OnSurfaceDark,
    surface            = SurfaceDark,
    onSurface          = OnSurfaceDark,
    surfaceVariant     = Slate700,
    onSurfaceVariant   = OnSurfaceVariantDark,
    outline            = Slate600,
    error              = Rose500,
)

val LocalSmartTripColors = staticCompositionLocalOf {
    SmartTripColors(isDark = false)
}

data class SmartTripColors(val isDark: Boolean) {
    val cardBackground   = if (isDark) CardDark else CardLight
    val gradient1        = if (isDark) Slate800  else Blue600
    val gradient2        = if (isDark) Slate900  else Blue700
    val accent           = Amber400
    val success          = Success500
    val warning          = Warning500
}

@Composable
fun SmartTripTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }

    val smartTripColors = SmartTripColors(isDark = darkTheme)

    CompositionLocalProvider(LocalSmartTripColors provides smartTripColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = SmartTripTypography,
            content     = content
        )
    }
}

