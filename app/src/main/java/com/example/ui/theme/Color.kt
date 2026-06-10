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
    get() = if (isDarkThemeGlobal) Color(0xA1142540) else Color(0xBFFAFAF9)

val NavyBackground: Color
    get() = if (isDarkThemeGlobal) Color(0xFF091223) else Color(0xFFFFFFFF)

val NavyDistant: Color
    get() = if (isDarkThemeGlobal) Color(0xFF2C436F) else Color(0xFFCBD5E1)

val CoralOrange: Color
    get() = if (isDarkThemeGlobal) Color(0xFF9EA7B6) else Color(0xFF4A5568) // Thunder grey for buttons and highlights

val GoldOrange: Color
    get() = if (isDarkThemeGlobal) Color(0xFF94A3B8) else Color(0xFF475569) // Grey accent

val BlueAccent = Color(0xFF00B4D8)        // Extra status accent color (e.g. system is OK)

val TextPrimary: Color
    get() = if (isDarkThemeGlobal) Color(0xFFF1F5F9) else Color(0xFF0C2340)

val TextMuted: Color
    get() = if (isDarkThemeGlobal) Color(0xFF94A3B8) else Color(0xFF64748B)
