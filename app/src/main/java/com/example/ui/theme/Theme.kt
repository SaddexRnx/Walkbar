package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val WalkbarDarkColorScheme = darkColorScheme(
  primary = AccentPrimary,
  onPrimary = Color.White,
  primaryContainer = AccentPrimaryMuted,
  onPrimaryContainer = AccentPrimaryLight,
  secondary = AccentGold,
  onSecondary = Color.Black,
  secondaryContainer = DarkSurfaceCard,
  onSecondaryContainer = TextPrimary,
  tertiary = AccentCyan,
  onTertiary = Color.Black,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkBorderSubtle
)

@Composable
fun WalkbarTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = WalkbarDarkColorScheme,
    typography = Typography,
    content = content
  )
}


