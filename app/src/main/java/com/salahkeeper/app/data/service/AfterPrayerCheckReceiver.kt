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
import com.salahkeeper.app.data.local.PrayerLogDao
import com.salahkeeper.app.data.local.PrayerLogEntity
import com.salahkeeper.app.data.model.PrayerStatus
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class AfterPrayerCheckReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prayerLogDao: PrayerLogDao

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val scheduledTime = intent.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())

        when (intent.action) {
            ACTION_POST_PRAYER_CHECK -> {
                showCheckNotification(context, prayerName, scheduledTime)
            }
            ACTION_RESPONSE_ON_TIME -> {
                handleUserResponse(context, prayerName, scheduledTime, PrayerStatus.ON_TIME)
            }
            ACTION_RESPONSE_LATE -> {
                handleUserResponse(context, prayerName, scheduledTime, PrayerStatus.LATE)
            }
            ACTION_RESPONSE_MISSED -> {
                handleUserResponse(context, prayerName, scheduledTime, PrayerStatus.MISSED)
            }
        }
    }

    private fun showCheckNotification(context: Context, prayerName: String, scheduledTime: Long) {
        val prayerType = PrayerType.fromName(prayerName)
        val notificationManager = context.getSystemService<NotificationManager>() ?: return

        fun createActionPendingIntent(actionName: String, reqCode: Int): PendingIntent {
            val intent = Intent(context, AfterPrayerCheckReceiver::class.java).apply {
                action = actionName
                putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
                putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
            }
            return PendingIntent.getBroadcast(
                context,
                reqCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val onTimePendingIntent = createActionPendingIntent(ACTION_RESPONSE_ON_TIME, 3001)
        val latePendingIntent = createActionPendingIntent(ACTION_RESPONSE_LATE, 3002)
        val missedPendingIntent = createActionPendingIntent(ACTION_RESPONSE_MISSED, 3003)

        // Open app if user taps notification body
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            3004,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SalahKeeperApp.CHECK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Did you pray ${prayerType.title}?")
            .setContentText("Record your prayer status to keep your streak going.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Yes (on time)", onTimePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Yes (late)", latePendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Missed", missedPendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_CHECK_BASE_ID + prayerType.ordinal, notification)
    }

    private fun handleUserResponse(
        context: Context,
        prayerName: String,
        scheduledTime: Long,
        status: PrayerStatus
    ) {
        val notificationManager = context.getSystemService<NotificationManager>()
        val prayerType = PrayerType.fromName(prayerName)
        notificationManager?.cancel(NOTIFICATION_CHECK_BASE_ID + prayerType.ordinal)

        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(scheduledTime))

        CoroutineScope(Dispatchers.IO).launch {
            prayerLogDao.insertOrUpdate(
                PrayerLogEntity(
                    dateString = dateString,
                    prayerName = prayerName,
                    scheduledTimeMillis = scheduledTime,
                    stoppedTimeMillis = System.currentTimeMillis(),
                    status = status.name
                )
            )
        }
    }

    companion object {
        const val ACTION_POST_PRAYER_CHECK = "com.salahkeeper.app.ACTION_POST_PRAYER_CHECK"
        const val ACTION_RESPONSE_ON_TIME = "com.salahkeeper.app.ACTION_RESPONSE_ON_TIME"
        const val ACTION_RESPONSE_LATE = "com.salahkeeper.app.ACTION_RESPONSE_LATE"
        const val ACTION_RESPONSE_MISSED = "com.salahkeeper.app.ACTION_RESPONSE_MISSED"
        private const val NOTIFICATION_CHECK_BASE_ID = 8000
    }
}
