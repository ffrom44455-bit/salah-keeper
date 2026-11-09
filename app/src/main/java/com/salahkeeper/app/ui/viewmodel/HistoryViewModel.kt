package com.salahkeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahkeeper.app.data.local.PrayerLogEntity
import com.salahkeeper.app.data.repository.AppSettings
import com.salahkeeper.app.data.repository.PrayerRepository
import com.salahkeeper.app.data.repository.SettingsRepository
import com.salahkeeper.app.data.repository.StreakStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val statistics: StateFlow<StreakStatistics?> = prayerRepository.getStatisticsFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val qazaList: StateFlow<List<PrayerLogEntity>> = prayerRepository.getQazaList().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    fun markQazaMadeUp(id: Long) {
        viewModelScope.launch {
            prayerRepository.fulfillQaza(id)
        }
    }

    fun updateCreatorName(name: String) {
        viewModelScope.launch {
            settingsRepository.updateCreatorName(name)
        }
    }
}
