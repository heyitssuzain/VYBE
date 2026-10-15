package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AetherDarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = CyanGlow,
    secondary = VioletNeon,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF38155A),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = MagentaGlow,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF500F31),
    onTertiaryContainer = Color(0xFFFCE7F3),
    background = AetherVoid,
    onBackground = StarlightHigh,
    surface = AetherSurface,
    onSurface = StarlightHigh,
    surfaceVariant = AetherSurfaceVariant,
    onSurfaceVariant = StarlightMedium,
    outline = AetherBorder,
    outlineVariant = Color(0xFF1E243A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AetherDarkColorScheme,
        typography = Typography,
        content = content
    )
}
