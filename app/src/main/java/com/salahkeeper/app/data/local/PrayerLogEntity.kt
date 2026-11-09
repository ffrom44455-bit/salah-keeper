package com.salahkeeper.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.salahkeeper.app.data.model.PrayerStatus
import com.salahkeeper.app.data.model.PrayerType

@Entity(
    tableName = "prayer_logs",
    indices = [
        Index(value = ["dateString", "prayerName"], unique = true),
        Index(value = ["dateString"]),
        Index(value = ["status"])
    ]
)
data class PrayerLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val prayerName: String, // FAJR, DHUHR, etc.
    val scheduledTimeMillis: Long,
    val stoppedTimeMillis: Long = 0L,
    val status: String = PrayerStatus.UPCOMING.name,
    val isQazaFulfilled: Boolean = false,
    val qazaFulfilledTimeMillis: Long = 0L
) {
    val prayerType: PrayerType
        get() = PrayerType.fromName(prayerName)

    val prayerStatus: PrayerStatus
        get() = try {
            PrayerStatus.valueOf(status)
        } catch (e: Exception) {
            PrayerStatus.UPCOMING
        }
}
