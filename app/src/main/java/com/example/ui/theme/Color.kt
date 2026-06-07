package com.example.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Active Compose State for Theme Choice
var isDarkThemeGlobal by mutableStateOf(true)

val NavyPrimary: Color
    get() = if (isDarkThemeGlobal) Color(0xFF0F1E36) else Color(0xFFE2E8F0)

val NavySurface: Color
    get() = if (isDarkThemeGlobal) Color(0xFF1B2E4C) else Color(0xFFFFFFFF)

val NavyBackground: Color
    get() = if (isDarkThemeGlobal) Color(0xFF091223) else Color(0xFFF8FAFC)

val NavyDistant: Color
    get() = if (isDarkThemeGlobal) Color(0xFF2C436F) else Color(0xFFCBD5E1)

val CoralOrange = Color(0xFFF15A24)       // Vibrant sunburst orange for buttons and highlights
val GoldOrange = Color(0xFFFF8C00)        // Secondary golden orange accent
val BlueAccent = Color(0xFF00B4D8)        // Extra status accent color (e.g. system is OK)

val TextPrimary: Color
    get() = if (isDarkThemeGlobal) Color(0xFFF1F5F9) else Color(0xFF0F172A)

val TextMuted: Color
    get() = if (isDarkThemeGlobal) Color(0xFF94A3B8) else Color(0xFF64748B)
