package com.example.ui.theme

import android.os.Build
import androidx.compose.ui.graphics.Color
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
    primary = ElegantDarkPrimary,
    onPrimary = ElegantDarkBg,
    primaryContainer = ElegantDarkAccent,
    onPrimaryContainer = ElegantDarkPrimary,
    secondary = ElegantDarkSecondary,
    onSecondary = ElegantDarkBg,
    tertiary = ElegantDarkAccent,
    onTertiary = ElegantDarkPrimary,
    background = ElegantDarkBg,
    onBackground = ElegantDarkText,
    surface = ElegantDarkSurface,
    onSurface = ElegantDarkText,
    surfaceVariant = ElegantDarkSurface,
    onSurfaceVariant = ElegantDarkSecondary,
    outline = ElegantDarkOutline,
    error = Color(0xFFFFB4AB),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
  )

private val LightColorScheme = DarkColorScheme // Elegant Dark is uniform

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme for Elegant Dark
  dynamicColor: Boolean = false, // Disable dynamic colors so our customized Elegant Dark is applied
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
