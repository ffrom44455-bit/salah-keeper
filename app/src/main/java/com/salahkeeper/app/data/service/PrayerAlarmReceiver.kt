package com.salahkeeper.app.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.ContextCompat
import com.salahkeeper.app.ui.alarm.AlarmActivity

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SalahKeeper:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(60 * 1000L) // 60 seconds

        // 1. Start foreground alarm service for continuous looping audio & ongoing notification
        val serviceIntent = Intent(context, PrayerAlarmService::class.java).apply {
            action = PrayerAlarmService.ACTION_START_ALARM
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        // 2. Launch full-screen AlarmActivity directly over any active app (e.g. Instagram, YouTube) or lockscreen
        val alarmActivityIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
        }
        try {
            context.startActivity(alarmActivityIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
