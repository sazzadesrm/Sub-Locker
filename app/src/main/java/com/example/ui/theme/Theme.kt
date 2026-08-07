package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkOnPrimary,
    primaryContainer = ElegantDarkPrimaryContainer,
    onPrimaryContainer = ElegantDarkOnPrimaryContainer,
    secondary = ElegantDarkPrimary,
    onSecondary = ElegantDarkOnPrimary,
    secondaryContainer = ElegantDarkSurfaceVariant,
    onSecondaryContainer = ElegantTextMain,
    tertiary = ElegantDarkPrimary,
    onTertiary = ElegantDarkOnPrimary,
    tertiaryContainer = ElegantDarkPrimaryContainer,
    onTertiaryContainer = ElegantDarkOnPrimaryContainer,
    background = ElegantDarkBg,
    onBackground = ElegantTextMain,
    surface = ElegantDarkSurface,
    onSurface = ElegantTextMain,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantTextVariant,
    outline = ElegantBorder,
    outlineVariant = ElegantTextMuted,
    error = ElegantAlertAccent,
    onError = ElegantDarkOnPrimary,
    errorContainer = ElegantAlertBg,
    onErrorContainer = ElegantAlertText
)

private val LightColorScheme = lightColorScheme(
    primary = ElegantLightPrimary,
    onPrimary = ElegantLightOnPrimary,
    primaryContainer = ElegantLightPrimaryContainer,
    onPrimaryContainer = ElegantLightOnPrimaryContainer,
    secondary = ElegantLightPrimary,
    onSecondary = ElegantLightOnPrimary,
    secondaryContainer = ElegantLightSurfaceVariant,
    onSecondaryContainer = Color(0xFF1D1B20),
    background = ElegantLightBg,
    onBackground = Color(0xFF1D1B20),
    surface = ElegantLightSurface,
    onSurface = Color(0xFF1D1B20),
    surfaceVariant = ElegantLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E)
)

@Composable
fun SubifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
