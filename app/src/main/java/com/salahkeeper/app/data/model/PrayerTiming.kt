package com.salahkeeper.app.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PrayerTiming(
    val type: PrayerType,
    val timeMillis: Long,
    val isEnabled: Boolean = true,
    val status: PrayerStatus = PrayerStatus.UPCOMING
) {
    val formattedTime: String
        get() {
            val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return formatter.format(Date(timeMillis))
        }

    val isPassed: Boolean
        get() = System.currentTimeMillis() > timeMillis
}
