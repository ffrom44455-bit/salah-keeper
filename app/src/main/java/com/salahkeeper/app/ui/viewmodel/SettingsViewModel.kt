package com.salahkeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahkeeper.app.data.model.CalculationMethodOption
import com.salahkeeper.app.data.model.CityPreset
import com.salahkeeper.app.data.model.MadhabOption
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.data.repository.AppSettings
import com.salahkeeper.app.data.repository.PrayerRepository
import com.salahkeeper.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val prayerRepository: PrayerRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun setCalculationMethod(method: CalculationMethodOption) {
        viewModelScope.launch {
            settingsRepository.updateCalculationMethod(method)
            prayerRepository.rescheduleAlarms()
        }
    }

    fun setMadhab(madhab: MadhabOption) {
        viewModelScope.launch {
            settingsRepository.updateMadhab(madhab)
            prayerRepository.rescheduleAlarms()
        }
    }

    fun selectCityPreset(preset: CityPreset) {
        viewModelScope.launch {
            settingsRepository.updateLocation(
                lat = preset.latitude,
                lon = preset.longitude,
                cityName = preset.displayName,
                useGps = false
            )
            prayerRepository.rescheduleAlarms()
        }
    }

    fun toggleGps(useGps: Boolean) {
        viewModelScope.launch {
            if (useGps) {
                prayerRepository.refreshLocationAndReschedule()
            } else {
                val current = settings.value
                settingsRepository.updateLocation(current.latitude, current.longitude, current.cityName, false)
            }
        }
    }

    fun togglePrayer(type: PrayerType, enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.togglePrayer(type, enabled)
            prayerRepository.rescheduleAlarms()
        }
    }

    fun updateManualTimes(
        enabled: Boolean,
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String
    ) {
        viewModelScope.launch {
            settingsRepository.updateManualTimes(enabled, fajr, dhuhr, asr, maghrib, isha)
            prayerRepository.rescheduleAlarms()
        }
    }

    fun setReduceEffects(reduce: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateReduceEffects(reduce)
        }
    }

    fun setDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateDarkMode(isDark)
        }
    }

    fun setCreatorName(name: String) {
        viewModelScope.launch {
            settingsRepository.updateCreatorName(name)
        }
    }

    fun triggerTestAlarm() {
        prayerRepository.triggerTestAlarm()
    }
}
