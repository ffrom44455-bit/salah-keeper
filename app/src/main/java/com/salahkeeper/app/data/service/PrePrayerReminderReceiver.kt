package com.salahkeeper.app.data.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.salahkeeper.app.R
import com.salahkeeper.app.SalahKeeperApp
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrePrayerReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val prayerType = PrayerType.fromName(prayerName)
        val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
        val reminderMinutes = intent.getIntExtra(EXTRA_REMINDER_MINUTES, 7)

        val notificationManager = context.getSystemService<NotificationManager>() ?: return

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            5001 + prayerType.ordinal,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(Date(scheduledTime))

        val notification = NotificationCompat.Builder(context, SalahKeeperApp.PRE_REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Preparing for ${prayerType.title} (${prayerType.arabicName})")
            .setContentText("${prayerType.title} begins in $reminderMinutes minutes ($timeStr). Time to prepare and make wudu.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_PRE_REMINDER_BASE_ID + prayerType.ordinal, notification)
    }

    companion object {
        const val ACTION_PRE_PRAYER_REMINDER = "com.salahkeeper.app.ACTION_PRE_PRAYER_REMINDER"
        const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"
        private const val NOTIFICATION_PRE_REMINDER_BASE_ID = 7000
    }
}
