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

private val RadioDarkColorScheme =
  darkColorScheme(
    primary = GeometricPrimary,
    onPrimary = GeometricOnPrimary,
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = GeometricPrimaryContainer,
    secondary = GeometricSecondary,
    onSecondary = GeometricOnSecondary,
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = GeometricSecondaryContainer,
    tertiary = GeometricPrimary,
    onTertiary = GeometricOnPrimary,
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E0E9),
    surface = Color(0xFF1D1B20),
    onSurface = Color(0xFFE6E0E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
  )

private val RadioLightColorScheme =
  lightColorScheme(
    primary = GeometricPrimary,
    onPrimary = GeometricOnPrimary,
    primaryContainer = GeometricPrimaryContainer,
    onPrimaryContainer = GeometricOnPrimaryContainer,
    secondary = GeometricSecondary,
    onSecondary = GeometricOnSecondary,
    secondaryContainer = GeometricSecondaryContainer,
    onSecondaryContainer = GeometricOnSecondaryContainer,
    tertiary = GeometricPrimary,
    onTertiary = GeometricOnPrimary,
    background = GeometricBg,
    onBackground = GeometricTextPrimary,
    surface = GeometricSurface,
    onSurface = GeometricTextPrimary,
    surfaceVariant = GeometricSurfaceVariant,
    onSurfaceVariant = GeometricTextSecondary,
    outline = GeometricBorder,
    outlineVariant = GeometricBorderLight
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Default to Geometric Balance light theme
  dynamicColor: Boolean = false, // Keep consistent Geometric Balance styling
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) RadioDarkColorScheme else RadioLightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
