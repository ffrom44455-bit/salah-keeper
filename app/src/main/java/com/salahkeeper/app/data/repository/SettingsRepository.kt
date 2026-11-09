package com.salahkeeper.app.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.salahkeeper.app.data.model.CalculationMethodOption
import com.salahkeeper.app.data.model.MadhabOption
import com.salahkeeper.app.data.model.PrayerType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "salah_keeper_prefs")

data class AppSettings(
    val calculationMethod: CalculationMethodOption = CalculationMethodOption.KARACHI,
    val madhab: MadhabOption = MadhabOption.HANAFI,
    val latitude: Double = 24.8607, // Default Karachi
    val longitude: Double = 67.0011,
    val cityName: String = "Karachi, Pakistan",
    val useGps: Boolean = true,
    val isFajrEnabled: Boolean = true,
    val isDhuhrEnabled: Boolean = true,
    val isAsrEnabled: Boolean = true,
    val isMaghribEnabled: Boolean = true,
    val isIshaEnabled: Boolean = true,
    val useManualTimes: Boolean = false,
    val manualFajrTime: String = "05:15",
    val manualDhuhrTime: String = "12:30",
    val manualAsrTime: String = "15:45",
    val manualMaghribTime: String = "18:10",
    val manualIshaTime: String = "19:30",
    val reduceEffects: Boolean = false,
    val isDarkMode: Boolean = true,
    val creatorName: String = "Salah Keeper User",
    val isOnboardingCompleted: Boolean = false
) {
    fun isPrayerEnabled(type: PrayerType): Boolean {
        return when (type) {
            PrayerType.FAJR -> isFajrEnabled
            PrayerType.DHUHR -> isDhuhrEnabled
            PrayerType.ASR -> isAsrEnabled
            PrayerType.MAGHRIB -> isMaghribEnabled
            PrayerType.ISHA -> isIshaEnabled
        }
    }
}

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val CALCULATION_METHOD = stringPreferencesKey("calc_method")
        val MADHAB = stringPreferencesKey("madhab")
        val LATITUDE = doublePreferencesKey("latitude")
        val LONGITUDE = doublePreferencesKey("longitude")
        val CITY_NAME = stringPreferencesKey("city_name")
        val USE_GPS = booleanPreferencesKey("use_gps")
        val FAJR_ENABLED = booleanPreferencesKey("fajr_enabled")
        val DHUHR_ENABLED = booleanPreferencesKey("dhuhr_enabled")
        val ASR_ENABLED = booleanPreferencesKey("asr_enabled")
        val MAGHRIB_ENABLED = booleanPreferencesKey("maghrib_enabled")
        val ISHA_ENABLED = booleanPreferencesKey("isha_enabled")
        val USE_MANUAL_TIMES = booleanPreferencesKey("use_manual_times")
        val MANUAL_FAJR = stringPreferencesKey("manual_fajr")
        val MANUAL_DHUHR = stringPreferencesKey("manual_dhuhr")
        val MANUAL_ASR = stringPreferencesKey("manual_asr")
        val MANUAL_MAGHRIB = stringPreferencesKey("manual_maghrib")
        val MANUAL_ISHA = stringPreferencesKey("manual_isha")
        val REDUCE_EFFECTS = booleanPreferencesKey("reduce_effects")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val CREATOR_NAME = stringPreferencesKey("creator_name")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data.map { prefs ->
        AppSettings(
            calculationMethod = CalculationMethodOption.fromName(
                prefs[PreferencesKeys.CALCULATION_METHOD] ?: CalculationMethodOption.KARACHI.name
            ),
            madhab = MadhabOption.fromName(
                prefs[PreferencesKeys.MADHAB] ?: MadhabOption.HANAFI.name
            ),
            latitude = prefs[PreferencesKeys.LATITUDE] ?: 24.8607,
            longitude = prefs[PreferencesKeys.LONGITUDE] ?: 67.0011,
            cityName = prefs[PreferencesKeys.CITY_NAME] ?: "Karachi, Pakistan",
            useGps = prefs[PreferencesKeys.USE_GPS] ?: true,
            isFajrEnabled = prefs[PreferencesKeys.FAJR_ENABLED] ?: true,
            isDhuhrEnabled = prefs[PreferencesKeys.DHUHR_ENABLED] ?: true,
            isAsrEnabled = prefs[PreferencesKeys.ASR_ENABLED] ?: true,
            isMaghribEnabled = prefs[PreferencesKeys.MAGHRIB_ENABLED] ?: true,
            isIshaEnabled = prefs[PreferencesKeys.ISHA_ENABLED] ?: true,
            useManualTimes = prefs[PreferencesKeys.USE_MANUAL_TIMES] ?: false,
            manualFajrTime = prefs[PreferencesKeys.MANUAL_FAJR] ?: "05:15",
            manualDhuhrTime = prefs[PreferencesKeys.MANUAL_DHUHR] ?: "12:30",
            manualAsrTime = prefs[PreferencesKeys.MANUAL_ASR] ?: "15:45",
            manualMaghribTime = prefs[PreferencesKeys.MANUAL_MAGHRIB] ?: "18:10",
            manualIshaTime = prefs[PreferencesKeys.MANUAL_ISHA] ?: "19:30",
            reduceEffects = prefs[PreferencesKeys.REDUCE_EFFECTS] ?: false,
            isDarkMode = prefs[PreferencesKeys.IS_DARK_MODE] ?: true,
            creatorName = prefs[PreferencesKeys.CREATOR_NAME] ?: "Salah Keeper User",
            isOnboardingCompleted = prefs[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun updateCalculationMethod(method: CalculationMethodOption) {
        dataStore.edit { it[PreferencesKeys.CALCULATION_METHOD] = method.name }
    }

    suspend fun updateMadhab(madhab: MadhabOption) {
        dataStore.edit { it[PreferencesKeys.MADHAB] = madhab.name }
    }

    suspend fun updateLocation(lat: Double, lon: Double, cityName: String, useGps: Boolean) {
        dataStore.edit {
            it[PreferencesKeys.LATITUDE] = lat
            it[PreferencesKeys.LONGITUDE] = lon
            it[PreferencesKeys.CITY_NAME] = cityName
            it[PreferencesKeys.USE_GPS] = useGps
        }
    }

    suspend fun togglePrayer(type: PrayerType, enabled: Boolean) {
        dataStore.edit { prefs ->
            when (type) {
                PrayerType.FAJR -> prefs[PreferencesKeys.FAJR_ENABLED] = enabled
                PrayerType.DHUHR -> prefs[PreferencesKeys.DHUHR_ENABLED] = enabled
                PrayerType.ASR -> prefs[PreferencesKeys.ASR_ENABLED] = enabled
                PrayerType.MAGHRIB -> prefs[PreferencesKeys.MAGHRIB_ENABLED] = enabled
                PrayerType.ISHA -> prefs[PreferencesKeys.ISHA_ENABLED] = enabled
            }
        }
    }

    suspend fun updateManualTimes(
        enabled: Boolean,
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String
    ) {
        dataStore.edit { prefs ->
            prefs[PreferencesKeys.USE_MANUAL_TIMES] = enabled
            prefs[PreferencesKeys.MANUAL_FAJR] = fajr
            prefs[PreferencesKeys.MANUAL_DHUHR] = dhuhr
            prefs[PreferencesKeys.MANUAL_ASR] = asr
            prefs[PreferencesKeys.MANUAL_MAGHRIB] = maghrib
            prefs[PreferencesKeys.MANUAL_ISHA] = isha
        }
    }

    suspend fun updateReduceEffects(reduce: Boolean) {
        dataStore.edit { it[PreferencesKeys.REDUCE_EFFECTS] = reduce }
    }

    suspend fun updateDarkMode(isDark: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_DARK_MODE] = isDark }
    }

    suspend fun updateCreatorName(name: String) {
        dataStore.edit { it[PreferencesKeys.CREATOR_NAME] = name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }
}
