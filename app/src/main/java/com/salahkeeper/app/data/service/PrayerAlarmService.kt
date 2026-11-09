package com.salahkeeper.app.data.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.salahkeeper.app.R
import com.salahkeeper.app.SalahKeeperApp
import com.salahkeeper.app.data.local.PrayerLogDao
import com.salahkeeper.app.data.local.PrayerLogEntity
import com.salahkeeper.app.data.model.PrayerStatus
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.ui.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class PrayerAlarmService : LifecycleService() {

    @Inject
    lateinit var audioPlayerManager: AudioPlayerManager

    @Inject
    lateinit var prayerLogDao: PrayerLogDao

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var currentPrayerName: String = "FAJR"
    private var scheduledTime: Long = 0L

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        when (intent?.action) {
            ACTION_STOP_ALARM -> {
                handleStopAlarm(isEmergency = false)
            }
            ACTION_EMERGENCY_STOP -> {
                handleStopAlarm(isEmergency = true)
            }
            ACTION_START_ALARM, null -> {
                currentPrayerName = intent?.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
                scheduledTime = intent?.getLongExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, System.currentTimeMillis())
                    ?: System.currentTimeMillis()

                startAlarmForeground()
            }
        }

        return START_NOT_STICKY
    }

    private fun startAlarmForeground() {
        val prayerType = PrayerType.fromName(currentPrayerName)

        // Full-screen Intent over lockscreen
        val fullScreenIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, currentPrayerName)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            )
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            1001,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Try direct launch so that if user is on Instagram or home screen, activity opens immediately
        try {
            startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Notification stop action
        val stopIntent = Intent(this, PrayerAlarmService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1002,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, SalahKeeperApp.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${prayerType.title} (${prayerType.arabicName}) Prayer Call")
            .setContentText(getString(R.string.alarm_ringing_desc))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.im_going_to_pray), fullScreenPendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, getString(R.string.emergency_stop), stopPendingIntent)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        audioPlayerManager.startAlarmCycle()
    }

    private fun handleStopAlarm(isEmergency: Boolean) {
        audioPlayerManager.stopAlarm()

        val stoppedTime = System.currentTimeMillis()
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(scheduledTime))

        serviceScope.launch {
            val status = if (isEmergency) PrayerStatus.UNCONFIRMED else PrayerStatus.UPCOMING
            prayerLogDao.insertOrUpdate(
                PrayerLogEntity(
                    dateString = dateString,
                    prayerName = currentPrayerName,
                    scheduledTimeMillis = scheduledTime,
                    stoppedTimeMillis = stoppedTime,
                    status = status.name
                )
            )

            // Schedule the 20-minute post-prayer check
            schedulePostPrayerCheck(this@PrayerAlarmService, currentPrayerName, scheduledTime)
            stopSelf()
        }
    }

    private fun schedulePostPrayerCheck(context: Context, prayerName: String, scheduledTime: Long) {
        val checkIntent = Intent(context, AfterPrayerCheckReceiver::class.java).apply {
            action = AfterPrayerCheckReceiver.ACTION_POST_PRAYER_CHECK
            putExtra(AlarmScheduler.EXTRA_PRAYER_NAME, prayerName)
            putExtra(AlarmScheduler.EXTRA_SCHEDULED_TIME, scheduledTime)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            2001,
            checkIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
        // 20 minutes from now (in ms: 20 * 60 * 1000 = 1,200,000)
        val triggerTime = System.currentTimeMillis() + (20 * 60 * 1000L)

        alarmManager?.let { am ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                am.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            } else {
                am.setExact(android.app.AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            }
        }
    }

    override fun onDestroy() {
        audioPlayerManager.stopAlarm()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    companion object {
        const val ACTION_START_ALARM = "com.salahkeeper.app.ACTION_START_ALARM"
        const val ACTION_STOP_ALARM = "com.salahkeeper.app.ACTION_STOP_ALARM"
        const val ACTION_EMERGENCY_STOP = "com.salahkeeper.app.ACTION_EMERGENCY_STOP"
        private const val NOTIFICATION_ID = 9001
    }
}
