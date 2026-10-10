package com.miliconfig.ipv6scanner.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = BrandGreenPrimary,
    onPrimary = BgDark,
    primaryContainer = BrandGreenDark,
    onPrimaryContainer = TextPrimary,
    secondary = BrandGreenGlow,
    onSecondary = BgDark,
    background = BgDark,
    onBackground = TextPrimary,
    surface = BgSurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder
)

@Composable
fun MiliconfigTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
