package com.salahkeeper.app.data.service

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.salahkeeper.app.data.model.PrayerTiming
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.data.repository.AppSettings
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdhanCalculationService @Inject constructor() {

    fun calculateDailyTimings(settings: AppSettings, targetDate: Date = Date()): List<PrayerTiming> {
        if (settings.useManualTimes) {
            return calculateManualTimings(settings, targetDate)
        }

        val coordinates = Coordinates(settings.latitude, settings.longitude)
        val dateComponents = DateComponents.from(targetDate)
        val params = settings.calculationMethod.adhanMethod.parameters
        params.madhab = settings.madhab.adhanMadhab

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        return listOf(
            PrayerTiming(
                type = PrayerType.FAJR,
                timeMillis = prayerTimes.fajr.time,
                isEnabled = settings.isFajrEnabled
            ),
            PrayerTiming(
                type = PrayerType.DHUHR,
                timeMillis = prayerTimes.dhuhr.time,
                isEnabled = settings.isDhuhrEnabled
            ),
            PrayerTiming(
                type = PrayerType.ASR,
                timeMillis = prayerTimes.asr.time,
                isEnabled = settings.isAsrEnabled
            ),
            PrayerTiming(
                type = PrayerType.MAGHRIB,
                timeMillis = prayerTimes.maghrib.time,
                isEnabled = settings.isMaghribEnabled
            ),
            PrayerTiming(
                type = PrayerType.ISHA,
                timeMillis = prayerTimes.isha.time,
                isEnabled = settings.isIshaEnabled
            )
        )
    }

    private fun calculateManualTimings(settings: AppSettings, targetDate: Date): List<PrayerTiming> {
        val cal = Calendar.getInstance().apply { time = targetDate }

        fun parseTimeToMillis(timeStr: String): Long {
            return try {
                val parts = timeStr.trim().split(":")
                val hour = parts[0].toInt()
                val min = parts[1].toInt()
                val prayerCal = Calendar.getInstance().apply {
                    time = targetDate
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, min)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                prayerCal.timeInMillis
            } catch (e: Exception) {
                cal.timeInMillis
            }
        }

        return listOf(
            PrayerTiming(
                type = PrayerType.FAJR,
                timeMillis = parseTimeToMillis(settings.manualFajrTime),
                isEnabled = settings.isFajrEnabled
            ),
            PrayerTiming(
                type = PrayerType.DHUHR,
                timeMillis = parseTimeToMillis(settings.manualDhuhrTime),
                isEnabled = settings.isDhuhrEnabled
            ),
            PrayerTiming(
                type = PrayerType.ASR,
                timeMillis = parseTimeToMillis(settings.manualAsrTime),
                isEnabled = settings.isAsrEnabled
            ),
            PrayerTiming(
                type = PrayerType.MAGHRIB,
                timeMillis = parseTimeToMillis(settings.manualMaghribTime),
                isEnabled = settings.isMaghribEnabled
            ),
            PrayerTiming(
                type = PrayerType.ISHA,
                timeMillis = parseTimeToMillis(settings.manualIshaTime),
                isEnabled = settings.isIshaEnabled
            )
        )
    }

    /**
     * Determines the current upcoming prayer and remaining milliseconds until it occurs.
     * If all today's prayers have passed, returns tomorrow's Fajr.
     */
    fun getNextPrayer(settings: AppSettings): Pair<PrayerTiming, Long> {
        val now = System.currentTimeMillis()
        val todayTimings = calculateDailyTimings(settings, Date(now))

        val upcomingToday = todayTimings.firstOrNull { it.timeMillis > now }
        if (upcomingToday != null) {
            val remaining = upcomingToday.timeMillis - now
            return Pair(upcomingToday, remaining)
        }

        // All passed today -> Next is tomorrow Fajr
        val tomorrowCal = Calendar.getInstance().apply {
            time = Date(now)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimings = calculateDailyTimings(settings, tomorrowCal.time)
        val tomorrowFajr = tomorrowTimings.first { it.type == PrayerType.FAJR }
        val remaining = tomorrowFajr.timeMillis - now
        return Pair(tomorrowFajr, remaining)
    }
}
