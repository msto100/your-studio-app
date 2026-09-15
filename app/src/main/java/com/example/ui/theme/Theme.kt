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

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF374151),
    onPrimaryContainer = Color(0xFFF3F4F6),
    secondary = StudioSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1F2937),
    onSecondaryContainer = Color(0xFFE5E7EB),
    tertiary = StudioTertiary,
    onTertiary = Color.Black,
    background = StudioDarkBg, // 0xFF111827 (bg-gray-900)
    onBackground = Color(0xFFF9FAFB),
    surface = StudioDarkSurface, // 0xFF1F2937 (bg-gray-800)
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = StudioDarkCard, // 0xFF1F2937 (bg-gray-800)
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = StudioDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = StudioPrimary,
    onPrimary = Color.White,
    secondary = StudioSecondary,
    onSecondary = Color.Black,
    tertiary = StudioTertiary,
    background = Color(0xFF111827),
    onBackground = Color(0xFFF9FAFB),
    surface = Color(0xFF1F2937),
    onSurface = Color(0xFFF9FAFB),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0xFF374151)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  // Dynamic color disabled for 60fps high performance & consistent solid dark theme
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
