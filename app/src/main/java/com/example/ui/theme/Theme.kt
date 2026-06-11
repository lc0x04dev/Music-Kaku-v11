package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = NeonCyan,
    secondary = NeonMagenta,
    tertiary = NeonLavender,
    background = DeepDarkBackground,
    surface = SurfaceDark,
    onPrimary = DeepDarkBackground,
    onSecondary = DeepDarkBackground,
    onTertiary = DeepDarkBackground,
    onBackground = TextWhite,
    onSurface = TextWhite
  )

private val LightColorScheme = DarkColorScheme // Force dark theme everywhere to ensure consistent brand experience

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark
  dynamicColor: Boolean = false, // Disable dynamic colors to keep neon theme
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
