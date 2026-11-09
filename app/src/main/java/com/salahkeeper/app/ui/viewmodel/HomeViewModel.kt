package com.salahkeeper.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salahkeeper.app.data.model.PrayerStatus
import com.salahkeeper.app.data.model.PrayerTiming
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.data.repository.PrayerRepository
import com.salahkeeper.app.data.repository.SettingsRepository
import com.salahkeeper.app.data.repository.TodayPrayerOverview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _tick = MutableStateFlow(System.currentTimeMillis())
    val tick: StateFlow<Long> = _tick.asStateFlow()

    init {
        // 1-second ticker for live smooth countdown
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                _tick.value = System.currentTimeMillis()
            }
        }
    }

    val todayOverview: StateFlow<TodayPrayerOverview?> = combine(
        prayerRepository.getTodayPrayerOverviewFlow(),
        _tick
    ) { overview, now ->
        val remaining = (overview.nextPrayer.timeMillis - now).coerceAtLeast(0L)
        overview.copy(remainingMillisToNext = remaining)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun updatePrayerStatus(timing: PrayerTiming, status: PrayerStatus) {
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            prayerRepository.updatePrayerStatus(todayStr, timing.type, status)
        }
    }

    fun refreshLocation() {
        viewModelScope.launch {
            prayerRepository.refreshLocationAndReschedule()
        }
    }
}
