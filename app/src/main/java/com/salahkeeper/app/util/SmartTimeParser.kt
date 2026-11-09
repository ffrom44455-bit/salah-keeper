package com.salahkeeper.app.util

import com.salahkeeper.app.data.model.PrayerType

data class ParsedTime(
    val hour24: Int,
    val minute: Int,
    val formatted12h: String,
    val formatted24h: String
)

object SmartTimeParser {

    /**
     * Intelligently parses user input into an exact 24-hour hour and minute using Islamic prayer context.
     * Supports:
     * - "5", "5am", "5:15", "5:15 AM", "5.15"
     * - "1", "1:30", "130", "1.30 pm"
     * - "8", "8:30", "830", "20:30", "8:30 pm"
     *
     * If AM/PM is omitted, automatically deduces AM/PM based on the Islamic prayer window:
     * - Fajr: always Morning (AM)
     * - Dhuhr: Midday (11 AM - 3 PM; e.g. 1..3 means PM, 12 means noon)
     * - Asr: Afternoon (PM)
     * - Maghrib: Sunset/Evening (PM)
     * - Isha: Night (PM)
     */
    fun parse(rawInput: String, prayerType: PrayerType): ParsedTime {
        val clean = rawInput.trim().lowercase()
        val hasAm = clean.contains("am") || clean.contains("a.m") || clean.endsWith("a")
        val hasPm = clean.contains("pm") || clean.contains("p.m") || clean.endsWith("p")

        // Strip non-digit, non-colon, non-dot characters
        val digitsOnly = clean.replace("[^0-9:.]".toRegex(), "")

        var rawHour = 0
        var rawMin = 0

        when {
            digitsOnly.contains(":") -> {
                val parts = digitsOnly.split(":")
                rawHour = parts.getOrNull(0)?.toIntOrNull() ?: defaultHour(prayerType)
                rawMin = parts.getOrNull(1)?.toIntOrNull() ?: 0
            }
            digitsOnly.contains(".") -> {
                val parts = digitsOnly.split(".")
                rawHour = parts.getOrNull(0)?.toIntOrNull() ?: defaultHour(prayerType)
                rawMin = parts.getOrNull(1)?.toIntOrNull() ?: 0
            }
            digitsOnly.length == 3 -> {
                // e.g. "530" -> 5:30, "130" -> 1:30
                rawHour = digitsOnly.substring(0, 1).toIntOrNull() ?: defaultHour(prayerType)
                rawMin = digitsOnly.substring(1, 3).toIntOrNull() ?: 0
            }
            digitsOnly.length == 4 -> {
                // e.g. "0530" -> 5:30, "1230" -> 12:30, "2030" -> 20:30
                rawHour = digitsOnly.substring(0, 2).toIntOrNull() ?: defaultHour(prayerType)
                rawMin = digitsOnly.substring(2, 4).toIntOrNull() ?: 0
            }
            digitsOnly.isNotEmpty() -> {
                // Just an hour typed, e.g. "5", "8", "12"
                rawHour = digitsOnly.toIntOrNull() ?: defaultHour(prayerType)
                rawMin = 0
            }
            else -> {
                rawHour = defaultHour(prayerType)
                rawMin = 0
            }
        }

        rawMin = rawMin.coerceIn(0, 59)

        // Determine 24-hour hour
        val finalHour: Int = when {
            hasAm -> {
                if (rawHour == 12) 0 else (rawHour % 24)
            }
            hasPm -> {
                if (rawHour < 12) rawHour + 12 else (rawHour % 24)
            }
            rawHour >= 12 -> {
                // Already in 24h format (e.g. 13..23)
                rawHour % 24
            }
            else -> {
                // SMART DEDUCTION BASED ON PRAYER CONTEXT
                when (prayerType) {
                    PrayerType.FAJR -> {
                        // Fajr is early morning: 1..11 -> AM
                        rawHour
                    }
                    PrayerType.DHUHR -> {
                        // 11 -> 11 AM, 12 -> 12 PM, 1..4 -> PM (13..16)
                        if (rawHour == 11 || rawHour == 12) rawHour else (rawHour + 12)
                    }
                    PrayerType.ASR -> {
                        // Asr is afternoon: 1..7 -> PM (13..19)
                        if (rawHour in 1..7) rawHour + 12 else rawHour
                    }
                    PrayerType.MAGHRIB -> {
                        // Maghrib is sunset: 1..9 -> PM (13..21)
                        if (rawHour in 1..9) rawHour + 12 else rawHour
                    }
                    PrayerType.ISHA -> {
                        // Isha is night: 1..11 -> PM (13..23)
                        if (rawHour in 1..11) rawHour + 12 else rawHour
                    }
                }
            }
        }

        val display12h = format12h(finalHour, rawMin)
        val display24h = String.format("%02d:%02d", finalHour, rawMin)

        return ParsedTime(
            hour24 = finalHour,
            minute = rawMin,
            formatted12h = display12h,
            formatted24h = display24h
        )
    }

    private fun defaultHour(prayerType: PrayerType): Int {
        return when (prayerType) {
            PrayerType.FAJR -> 5
            PrayerType.DHUHR -> 12
            PrayerType.ASR -> 16
            PrayerType.MAGHRIB -> 18
            PrayerType.ISHA -> 20
        }
    }

    fun format12h(hour24: Int, minute: Int): String {
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when (val h = hour24 % 12) {
            0 -> 12
            else -> h
        }
        return String.format("%d:%02d %s", hour12, minute, amPm)
    }
}
