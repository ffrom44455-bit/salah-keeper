package com.salahkeeper.app.data.service

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.data.repository.AppSettings
import com.salahkeeper.app.ui.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val calculationService: AdhanCalculationService
) {
    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAllPrayerAlarms(settings: AppSettings) {
        if (alarmManager == null) return

        val now = System.currentTimeMillis()

        // Schedule today's remaining prayers
        val todayTimings = calculationService.calculateDailyTimings(settings, Date(now))
        for (timing in todayTimings) {
            if (timing.isEnabled && timing.timeMillis > now) {
                scheduleExactAlarm(timing.type, timing.timeMillis)
            }
            if (timing.isEnabled && settings.isPreReminderEnabled) {
                val reminderTime = timing.timeMillis - (settings.preReminderMinutes * 60 * 1000L)
                if (reminderTime > now) {
                    schedulePreReminder(timing.type, timing.timeMillis, reminderTime, settings.preReminderMinutes)
                }
            }
        }

        // Also schedule tomorrow's prayers to guarantee coverage across midnight
        val tomorrowCal = Calendar.getInstance().apply {
            time = Date(now)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        val tomorrowTimings = calculationService.calculateDailyTimings(settings, tomorrowCal.time)
        for (timing in tomorrowTimings) {
            if (timing.isEnabled) {
                scheduleExactAlarm(timing.type, timing.timeMillis)
                if (settings.isPreReminderEnabled) {
                    val reminderTime = timing.timeMillis - (settings.preReminderMinutes * 60 * 1000L)
                    schedulePreReminder(timing.type, timing.timeMillis, reminderTime, settings.preReminderMinutes)
                }
            }
        }
    }

    fun schedulePreReminder(
        prayerType: PrayerType,
        scheduledTime: Long,
        triggerAtMillis: Long,
        reminderMinutes: Int
    ) {
        if (alarmManager == null) return

        val intent = Intent(context, PrePrayerReminderReceiver::class.java).apply {
            action = PrePrayerReminderReceiver.ACTION_PRE_PRAYER_REMINDER
            putExtra(EXTRA_PRAYER_NAME, prayerType.name)
            putExtra(EXTRA_SCHEDULED_TIME, scheduledTime)
            putExtra(PrePrayerReminderReceiver.EXTRA_REMINDER_MINUTES, reminderMinutes)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            prayerType.ordinal + 300,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleExactAlarm(prayerType: PrayerType, triggerAtMillis: Long) {
        if (alarmManager == null) return

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = ACTION_PRAYER_ALARM
            putExtra(EXTRA_PRAYER_NAME, prayerType.name)
            putExtra(EXTRA_SCHEDULED_TIME, triggerAtMillis)
        }

        val requestCode = prayerType.ordinal + 100
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = Intent(context, MainActivity::class.java)
        val showPendingIntent = PendingIntent.getActivity(
            context,
            requestCode + 1000,
            showIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, showPendingIntent)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                }
            } else {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        } catch (e: SecurityException) {
            // In case exact alarm permission was revoked, fallback to window/inexact
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    /**
     * Schedules a test alarm for 5 seconds from now so the user can verify the full cycle.
     */
    fun scheduleTestAlarm() {
        val triggerAt = System.currentTimeMillis() + 5000L
        scheduleExactAlarm(PrayerType.FAJR, triggerAt)
    }

    fun cancelAllAlarms() {
        if (alarmManager == null) return
        for (type in PrayerType.entries) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALARM
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                type.ordinal + 100,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    companion object {
        const val ACTION_PRAYER_ALARM = "com.salahkeeper.app.ACTION_PRAYER_ALARM"
        const val EXTRA_PRAYER_NAME = "extra_prayer_name"
        const val EXTRA_SCHEDULED_TIME = "extra_scheduled_time"
    }
}
