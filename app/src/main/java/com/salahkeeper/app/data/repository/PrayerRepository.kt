package com.salahkeeper.app.data.repository

import com.salahkeeper.app.data.local.PrayerLogDao
import com.salahkeeper.app.data.local.PrayerLogEntity
import com.salahkeeper.app.data.model.PrayerStatus
import com.salahkeeper.app.data.model.PrayerTiming
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.data.service.AdhanCalculationService
import com.salahkeeper.app.data.service.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class TodayPrayerOverview(
    val timings: List<PrayerTiming>,
    val nextPrayer: PrayerTiming,
    val remainingMillisToNext: Long,
    val currentStreak: Int,
    val encouragingMessage: String
)

data class StreakStatistics(
    val currentStreak: Int,
    val longestStreak: Int,
    val weeklyPercentage: Int,
    val monthlyPercentage: Int,
    val totalPrayedOnTime: Int,
    val totalPrayedLate: Int,
    val totalMissed: Int,
    val calendarHeatmap: Map<String, Float> // dateString -> ratio (0.0f to 1.0f)
)

@Singleton
class PrayerRepository @Inject constructor(
    private val prayerLogDao: PrayerLogDao,
    private val calculationService: AdhanCalculationService,
    private val settingsRepository: SettingsRepository,
    private val locationRepository: LocationRepository,
    private val alarmScheduler: AlarmScheduler
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun getTodayPrayerOverviewFlow(): Flow<TodayPrayerOverview> {
        val todayStr = dateFormat.format(Date())

        return combine(
            settingsRepository.settingsFlow,
            prayerLogDao.getLogsForDate(todayStr),
            prayerLogDao.getAllLogs()
        ) { settings, todayLogs, allLogs ->
            val dailyTimings = calculationService.calculateDailyTimings(settings, Date())
            val mappedTimings = dailyTimings.map { timing ->
                val log = todayLogs.firstOrNull { it.prayerName == timing.type.name }
                val status = when {
                    log != null -> log.prayerStatus
                    timing.isPassed -> PrayerStatus.MISSED
                    else -> PrayerStatus.UPCOMING
                }
                timing.copy(status = status)
            }

            val (nextPrayer, remainingMillis) = calculationService.getNextPrayer(settings)
            val currentStreak = calculateCurrentStreak(allLogs)
            val encouragingMessage = getEncouragingMessage(currentStreak)

            TodayPrayerOverview(
                timings = mappedTimings,
                nextPrayer = nextPrayer,
                remainingMillisToNext = remainingMillis,
                currentStreak = currentStreak,
                encouragingMessage = encouragingMessage
            )
        }
    }

    fun getStatisticsFlow(): Flow<StreakStatistics> {
        return prayerLogDao.getAllLogs().mapToStats()
    }

    private fun Flow<List<PrayerLogEntity>>.mapToStats(): Flow<StreakStatistics> {
        return kotlinx.coroutines.flow.map { logs ->
            val currentStreak = calculateCurrentStreak(logs)
            val longestStreak = calculateLongestStreak(logs)

            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()

            // Weekly (last 7 days)
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -7)
            val sevenDaysAgo = cal.timeInMillis

            val last7Logs = logs.filter { it.scheduledTimeMillis >= sevenDaysAgo }
            val weeklyPrayed = last7Logs.count { it.prayerStatus == PrayerStatus.ON_TIME || it.prayerStatus == PrayerStatus.LATE || it.prayerStatus == PrayerStatus.MADE_UP }
            val weeklyPercentage = if (last7Logs.isNotEmpty()) {
                ((weeklyPrayed.toFloat() / (7 * 5)) * 100).toInt().coerceIn(0, 100)
            } else 0

            // Monthly (last 30 days)
            cal.timeInMillis = now
            cal.add(Calendar.DAY_OF_YEAR, -30)
            val thirtyDaysAgo = cal.timeInMillis

            val last30Logs = logs.filter { it.scheduledTimeMillis >= thirtyDaysAgo }
            val monthlyPrayed = last30Logs.count { it.prayerStatus == PrayerStatus.ON_TIME || it.prayerStatus == PrayerStatus.LATE || it.prayerStatus == PrayerStatus.MADE_UP }
            val monthlyPercentage = if (last30Logs.isNotEmpty()) {
                ((monthlyPrayed.toFloat() / (30 * 5)) * 100).toInt().coerceIn(0, 100)
            } else 0

            // Heatmap: dateString to score (0.0 to 1.0)
            val heatmap = logs.groupBy { it.dateString }.mapValues { (_, dayLogs) ->
                val successCount = dayLogs.count { it.prayerStatus == PrayerStatus.ON_TIME || it.prayerStatus == PrayerStatus.LATE || it.prayerStatus == PrayerStatus.MADE_UP }
                (successCount.toFloat() / 5f).coerceIn(0f, 1f)
            }

            StreakStatistics(
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                weeklyPercentage = weeklyPercentage,
                monthlyPercentage = monthlyPercentage,
                totalPrayedOnTime = logs.count { it.prayerStatus == PrayerStatus.ON_TIME },
                totalPrayedLate = logs.count { it.prayerStatus == PrayerStatus.LATE },
                totalMissed = logs.count { it.prayerStatus == PrayerStatus.MISSED && !it.isQazaFulfilled },
                calendarHeatmap = heatmap
            )
        }
    }

    private fun calculateCurrentStreak(logs: List<PrayerLogEntity>): Int {
        if (logs.isEmpty()) return 0

        val cal = Calendar.getInstance()
        var streak = 0
        val grouped = logs.groupBy { it.dateString }

        // Start from today or yesterday
        for (i in 0..365) {
            val dateStr = dateFormat.format(cal.time)
            val dayLogs = grouped[dateStr]

            if (dayLogs != null && dayLogs.any { it.prayerStatus == PrayerStatus.ON_TIME || it.prayerStatus == PrayerStatus.LATE || it.prayerStatus == PrayerStatus.MADE_UP }) {
                streak++
            } else if (i > 0) {
                // If today has no logs yet, don't break streak immediately, check yesterday
                break
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return streak
    }

    private fun calculateLongestStreak(logs: List<PrayerLogEntity>): Int {
        if (logs.isEmpty()) return 0
        val datesWithPrayer = logs
            .filter { it.prayerStatus == PrayerStatus.ON_TIME || it.prayerStatus == PrayerStatus.LATE || it.prayerStatus == PrayerStatus.MADE_UP }
            .map { it.dateString }
            .distinct()
            .sorted()

        if (datesWithPrayer.isEmpty()) return 0

        var maxStreak = 1
        var currentStreak = 1
        for (i in 1 until datesWithPrayer.size) {
            try {
                val prev = dateFormat.parse(datesWithPrayer[i - 1])
                val curr = dateFormat.parse(datesWithPrayer[i])
                if (prev != null && curr != null) {
                    val diff = (curr.time - prev.time) / (1000 * 60 * 60 * 24)
                    if (diff == 1L) {
                        currentStreak++
                        if (currentStreak > maxStreak) {
                            maxStreak = currentStreak
                        }
                    } else if (diff > 1L) {
                        currentStreak = 1
                    }
                }
            } catch (e: Exception) {
                // ignore parsing error
            }
        }
        return maxStreak
    }

    private fun getEncouragingMessage(streak: Int): String {
        return when {
            streak == 0 -> "Take a breath. Every prayer is a fresh beginning."
            streak == 1 -> "1 day streak started! Beautiful beginning."
            streak in 2..6 -> "$streak days in a row, keep going! You got this."
            streak in 7..13 -> "1 whole week of devotion! Masha\'Allah."
            streak in 14..29 -> "$streak days unbroken! Your consistency is inspiring."
            streak in 30..99 -> "Over a month strong! May Allah bless your steadfastness."
            else -> "$streak days! Incredible journey of faith."
        }
    }

    suspend fun updatePrayerStatus(dateString: String, prayerType: PrayerType, status: PrayerStatus) {
        val existing = prayerLogDao.getLogForPrayer(dateString, prayerType.name)
        val log = existing?.copy(status = status.name) ?: PrayerLogEntity(
            dateString = dateString,
            prayerName = prayerType.name,
            scheduledTimeMillis = System.currentTimeMillis(),
            stoppedTimeMillis = System.currentTimeMillis(),
            status = status.name
        )
        prayerLogDao.insertOrUpdate(log)
    }

    fun getQazaList(): Flow<List<PrayerLogEntity>> {
        return prayerLogDao.getUnfulfilledQazaList()
    }

    suspend fun fulfillQaza(id: Long) {
        prayerLogDao.markQazaFulfilled(id, System.currentTimeMillis())
    }

    suspend fun refreshLocationAndReschedule(): Boolean {
        val loc = locationRepository.getCurrentLocation()
        if (loc.isSuccess) {
            settingsRepository.updateLocation(loc.latitude, loc.longitude, loc.cityName, true)
            val settings = settingsRepository.settingsFlow.first()
            alarmScheduler.scheduleAllPrayerAlarms(settings)
            return true
        }
        return false
    }

    suspend fun rescheduleAlarms() {
        val settings = settingsRepository.settingsFlow.first()
        alarmScheduler.scheduleAllPrayerAlarms(settings)
    }

    fun triggerTestAlarm() {
        alarmScheduler.scheduleTestAlarm()
    }
}
