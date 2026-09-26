package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Brand Colors
val BSBBlue = Color(0xFF123B6D)
val BSBBlueLight = Color(0xFF1D4E89)
val BSBAccent = Color(0xFFF28C28)
val BSBDeepNavy = Color(0xFF0B2545)

// Light Mode Colors
val LightBackground = Color(0xFFFFFFFF)
val LightSecondaryBackground = Color(0xFFF4F6F8)
val LightTextPrimary = Color(0xFF172B4D)
val LightTextSecondary = Color(0xFF4B5563)
val LightTextMuted = Color(0xFF6B7280)

// Dark Mode Colors
val NavyBackground = Color(0xFF001F3F)
val NavySurface = Color(0xFF002D5A)
val NavyPrimary = Color(0xFF003D7A)
val NavyDistant = Color(0xFF004D9A)
val CoralOrange = Color(0xFFF15A24)
val GoldOrange = Color(0xFFFFB300)

// Dynamic Colors based on global theme state
val TextPrimary: Color
    get() = if (isDarkThemeGlobal) Color(0xFFFFFFFF) else LightTextPrimary

val TextMuted: Color
    get() = if (isDarkThemeGlobal) Color(0xFFB0C4DE) else LightTextMuted

val BlueAccent = Color(0xFF00B4D8)
