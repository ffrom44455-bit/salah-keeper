package com.salahkeeper.app.data.model

import androidx.compose.ui.graphics.Color

enum class PrayerStatus(
    val displayName: String,
    val dotColor: Color
) {
    UPCOMING("Upcoming", Color(0xFF94A3B8)), // Grey
    ON_TIME("On Time", Color(0xFF10B981)),   // Glowing Green
    LATE("Late", Color(0xFFF59E0B)),         // Glowing Gold
    MISSED("Missed", Color(0xFFEF4444)),     // Glowing Red
    UNCONFIRMED("Unconfirmed", Color(0xFFA855F7)), // Purple
    MADE_UP("Made Up", Color(0xFF06B6D4))    // Cyan
}
