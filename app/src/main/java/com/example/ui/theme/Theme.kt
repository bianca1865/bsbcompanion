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

private val DarkColorScheme =
  darkColorScheme(
    primary = CoralOrange,
    secondary = GoldOrange,
    tertiary = BlueAccent,
    background = Color(0xFF091223),
    surface = Color(0xFF1B2E4C),
    onPrimary = Color(0xFF091223),
    onSecondary = Color(0xFF091223),
    onTertiary = Color(0xFFF1F5F9),
    onBackground = Color(0xFFF1F5F9),
    onSurface = Color(0xFFF1F5F9)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = CoralOrange,
    secondary = GoldOrange,
    tertiary = BlueAccent,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF8FAFC),
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF0C2340),
    onBackground = Color(0xFF0C2340),
    onSurface = Color(0xFF0C2340)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Override dynamicColor to false, preserving brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  isDarkThemeGlobal = darkTheme
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
