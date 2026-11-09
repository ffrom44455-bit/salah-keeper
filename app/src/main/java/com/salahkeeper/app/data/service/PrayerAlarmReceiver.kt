package com.salahkeeper.app.data.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import androidx.core.content.ContextCompat

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "SalahKeeper:AlarmReceiverWakeLock"
        )
        wakeLock?.acquire(15 * 1000L) // 15 seconds

        val serviceIntent = Intent(context, PrayerAlarmService::class.java).apply {
            action = PrayerAlarmService.ACTION_START_ALARM
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }

        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
