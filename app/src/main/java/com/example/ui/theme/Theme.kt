package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TacticalColorScheme = darkColorScheme(
    primary = TacticalEmerald,
    onPrimary = TacticalBlack,
    primaryContainer = TacticalEmeraldDim,
    onPrimaryContainer = TacticalTextPrimary,
    secondary = TacticalGold,
    onSecondary = TacticalBlack,
    secondaryContainer = TacticalGoldDim,
    onSecondaryContainer = TacticalTextPrimary,
    tertiary = TacticalCyan,
    onTertiary = TacticalBlack,
    background = TacticalBlack,
    onBackground = TacticalTextPrimary,
    surface = TacticalDarkSurface,
    onSurface = TacticalTextPrimary,
    surfaceVariant = TacticalCardSurface,
    onSurfaceVariant = TacticalTextSecondary,
    outline = TacticalCardBorder,
    outlineVariant = TacticalCardBorderActive,
    error = TacticalAlertRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    // Military tactical suite defaults consistently to the high-contrast stealth dark tactical scheme
    MaterialTheme(
        colorScheme = TacticalColorScheme,
        typography = Typography,
        content = content
    )
}
