package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

var isDarkThemeGlobal by mutableStateOf(false)

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
    primary = BSBBlue,
    secondary = BSBAccent,
    tertiary = BSBDeepNavy,
    background = LightBackground,
    surface = LightSecondaryBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightTextMuted,
    surfaceVariant = Color.White,
    onSurfaceVariant = LightTextSecondary
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
