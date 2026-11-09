package com.salahkeeper.app.data.model

import androidx.compose.ui.graphics.Color

enum class PrayerType(
    val title: String,
    val arabicName: String,
    val gradientStart: Color,
    val gradientEnd: Color
) {
    FAJR(
        title = "Fajr",
        arabicName = "الفجر",
        gradientStart = Color(0xFF1E1B4B), // Deep Indigo
        gradientEnd = Color(0xFFF472B6)    // Soft Pink
    ),
    DHUHR(
        title = "Dhuhr",
        arabicName = "الظهر",
        gradientStart = Color(0xFFEAB308), // Warm Gold
        gradientEnd = Color(0xFF0284C7)    // Sky Blue
    ),
    ASR(
        title = "Asr",
        arabicName = "العصر",
        gradientStart = Color(0xFFD97706), // Amber
        gradientEnd = Color(0xFFFB923C)    // Peach
    ),
    MAGHRIB(
        title = "Maghrib",
        arabicName = "المغرب",
        gradientStart = Color(0xFFEA580C), // Orange
        gradientEnd = Color(0xFF7C3AED)    // Purple
    ),
    ISHA(
        title = "Isha",
        arabicName = "العشاء",
        gradientStart = Color(0xFF0F172A), // Midnight Blue
        gradientEnd = Color(0xFF0D9488)    // Teal
    );

    companion object {
        fun fromName(name: String): PrayerType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: FAJR
        }
    }
}
