package com.salahkeeper.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SalahKeeperApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService<NotificationManager>() ?: return

            // Channel for the foreground prayer alarm
            val alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Prayer Alarm",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent continuous prayer alarm notifications"
                setSound(alarmSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                lockscreenVisibility = NotificationManager.IMPORTANCE_HIGH
            }

            // Channel for the 20-minute post-prayer check
            val checkChannel = NotificationChannel(
                CHECK_CHANNEL_ID,
                "Post-Prayer Check",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Follow-up reminders to log your prayer"
                enableVibration(true)
            }

            notificationManager.createNotificationChannels(listOf(alarmChannel, checkChannel))
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "salah_keeper_prayer_alarm"
        const val CHECK_CHANNEL_ID = "salah_keeper_post_check"
    }
}
