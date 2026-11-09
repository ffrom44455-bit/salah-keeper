package com.salahkeeper.app.data.model

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab

enum class CalculationMethodOption(
    val title: String,
    val adhanMethod: CalculationMethod
) {
    KARACHI("University of Islamic Sciences, Karachi", CalculationMethod.KARACHI),
    MUSLIM_WORLD_LEAGUE("Muslim World League", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    EGYPTIAN("Egyptian General Authority of Survey", CalculationMethod.EGYPTIAN),
    UMM_AL_QURA("Umm Al-Qura University, Makkah", CalculationMethod.UMM_AL_QURA),
    NORTH_AMERICA("ISNA (North America)", CalculationMethod.NORTH_AMERICA),
    DUBAI("Dubai", CalculationMethod.DUBAI),
    QATAR("Qatar", CalculationMethod.QATAR),
    KUWAIT("Kuwait", CalculationMethod.KUWAIT),
    SINGAPORE("Singapore", CalculationMethod.SINGAPORE),
    MOON_SIGHTING_COMMITTEE("Moonsighting Committee", CalculationMethod.MOON_SIGHTING_COMMITTEE);

    companion object {
        fun fromName(name: String): CalculationMethodOption {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: KARACHI
        }
    }
}

enum class MadhabOption(
    val title: String,
    val adhanMadhab: Madhab
) {
    HANAFI("Hanafi (Later Asr)", Madhab.HANAFI),
    SHAFI("Shafi\'i / Maliki / Hanbali (Standard Asr)", Madhab.SHAFI);

    companion object {
        fun fromName(name: String): MadhabOption {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: HANAFI
        }
    }
}

data class CityPreset(
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val displayName: String get() = "$name, $country"

    companion object {
        val PRESETS = listOf(
            CityPreset("Karachi", "Pakistan", 24.8607, 67.0011),
            CityPreset("Lahore", "Pakistan", 31.5204, 74.3587),
            CityPreset("Islamabad", "Pakistan", 33.6844, 73.0479),
            CityPreset("Mecca", "Saudi Arabia", 21.4225, 39.8262),
            CityPreset("Medina", "Saudi Arabia", 24.5247, 39.5692),
            CityPreset("Riyadh", "Saudi Arabia", 24.7136, 46.6753),
            CityPreset("Dubai", "UAE", 25.2048, 55.2708),
            CityPreset("Abu Dhabi", "UAE", 24.4539, 54.3773),
            CityPreset("Doha", "Qatar", 25.2854, 51.5310),
            CityPreset("Cairo", "Egypt", 30.0444, 31.2357),
            CityPreset("Istanbul", "Turkey", 41.0082, 28.9784),
            CityPreset("London", "United Kingdom", 51.5074, -0.1278),
            CityPreset("New York", "United States", 40.7128, -74.0060),
            CityPreset("Toronto", "Canada", 43.6532, -79.3832),
            CityPreset("Kuala Lumpur", "Malaysia", 3.1390, 101.6869),
            CityPreset("Jakarta", "Indonesia", -6.2088, 106.8456),
            CityPreset("Dhaka", "Bangladesh", 23.8103, 90.4125),
            CityPreset("Mumbai", "India", 19.0760, 72.8777),
            CityPreset("Delhi", "India", 28.6139, 77.2090),
            CityPreset("Sydney", "Australia", -33.8688, 151.2093)
        )
    }
}
