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
        fun toMillis(timeStr: String, type: PrayerType): Long {
            val parsed = com.salahkeeper.app.util.SmartTimeParser.parse(timeStr, type)
            val prayerCal = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, parsed.hour24)
                set(Calendar.MINUTE, parsed.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return prayerCal.timeInMillis
        }

        return listOf(
            PrayerTiming(
                type = PrayerType.FAJR,
                timeMillis = toMillis(settings.manualFajrTime, PrayerType.FAJR),
                isEnabled = settings.isFajrEnabled
            ),
            PrayerTiming(
                type = PrayerType.DHUHR,
                timeMillis = toMillis(settings.manualDhuhrTime, PrayerType.DHUHR),
                isEnabled = settings.isDhuhrEnabled
            ),
            PrayerTiming(
                type = PrayerType.ASR,
                timeMillis = toMillis(settings.manualAsrTime, PrayerType.ASR),
                isEnabled = settings.isAsrEnabled
            ),
            PrayerTiming(
                type = PrayerType.MAGHRIB,
                timeMillis = toMillis(settings.manualMaghribTime, PrayerType.MAGHRIB),
                isEnabled = settings.isMaghribEnabled
            ),
            PrayerTiming(
                type = PrayerType.ISHA,
                timeMillis = toMillis(settings.manualIshaTime, PrayerType.ISHA),
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
