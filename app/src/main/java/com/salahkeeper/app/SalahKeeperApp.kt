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

            // Channel for the foreground prayer alarm using bundled custom adhan
            val alarmSoundUri = android.net.Uri.parse("android.resource://${packageName}/${R.raw.adhan_alarm}")
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
                vibrationPattern = longArrayOf(0, 800, 400, 800, 400)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setBypassDnd(true)
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

            // Channel for the 7-minute pre-prayer preparation reminder
            val preReminderChannel = NotificationChannel(
                PRE_REMINDER_CHANNEL_ID,
                "Pre-Prayer Preparation Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications 7 minutes before prayer to make wudu and prepare"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
            }

            notificationManager.createNotificationChannels(listOf(alarmChannel, checkChannel, preReminderChannel))
        }
    }

    companion object {
        const val ALARM_CHANNEL_ID = "salah_keeper_prayer_alarm"
        const val CHECK_CHANNEL_ID = "salah_keeper_post_check"
        const val PRE_REMINDER_CHANNEL_ID = "salah_keeper_pre_reminder"
    }
}
