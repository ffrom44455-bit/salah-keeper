package com.salahkeeper.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.salahkeeper.app.ui.components.GlassCard
import com.salahkeeper.app.ui.theme.GlowGreen
import com.salahkeeper.app.util.HapticFeedbackUtil
import dev.chrisbanes.haze.HazeState

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var hasNotificationPerm by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED
            } else true
        )
    }

    var hasRecordAudioPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED
        )
    }

    var hasLocationPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PermissionChecker.PERMISSION_GRANTED
        )
    }

    val requestNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPerm = granted
    }

    val requestAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasRecordAudioPerm = granted
    }

    val requestLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { map ->
        hasLocationPerm = map[Manifest.permission.ACCESS_FINE_LOCATION] == true || map[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val hasBatteryExemption = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? android.app.AlarmManager
    val hasExactAlarmPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() == true
    } else true

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Welcome to Salah Keeper",
            color = Color.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "A few essential permissions ensure your prayer alarm always rings on time, even when the phone is locked.",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(26.dp))

        // 1. Notifications
        PermissionOnboardingCard(
            title = "1. Notifications",
            description = "Maintains the continuous prayer alarm and provides follow-up reminder check-ins.",
            icon = Icons.Rounded.Notifications,
            isGranted = hasNotificationPerm,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Microphone (Voice Dua Recognition)
        PermissionOnboardingCard(
            title = "2. Microphone (Voice Dua Recitation)",
            description = "Listens locally to your voice when reciting the Dua aloud to confirm and stop the alarm.",
            icon = Icons.Rounded.Mic,
            isGranted = hasRecordAudioPerm,
            onRequest = { requestAudioLauncher.launch(Manifest.permission.RECORD_AUDIO) },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Location for Prayer Times
        PermissionOnboardingCard(
            title = "3. Location",
            description = "Calculates exact local prayer times offline using the Adhan algorithm.",
            icon = Icons.Rounded.Alarm,
            isGranted = hasLocationPerm,
            onRequest = {
                requestLocationLauncher.launch(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                )
            },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Exact Alarm & Lockscreen
        PermissionOnboardingCard(
            title = "4. Exact Alarms & Lock Screen",
            description = "Bypasses Doze mode to ring on the exact second and awaken the screen.",
            icon = Icons.Rounded.PhoneAndroid,
            isGranted = hasExactAlarmPerm,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                }
            },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Battery Optimization Exemption
        PermissionOnboardingCard(
            title = "5. Battery Optimization Exemption",
            description = "Protects the alarm from aggressive OS background task killers.",
            icon = Icons.Rounded.BatteryChargingFull,
            isGranted = hasBatteryExemption,
            onRequest = {
                try {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    context.startActivity(intent)
                }
            },
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Get Started Button
        Button(
            onClick = {
                HapticFeedbackUtil.performSuccess(context)
                onComplete()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Enter Salah Keeper",
                color = Color(0xFF0F172A),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PermissionOnboardingCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    onRequest: () -> Unit,
    hazeState: HazeState? = null
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(18.dp),
        cornerRadius = 18.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) GlowGreen else Color(0xFF38BDF8),
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            if (isGranted) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Granted",
                    tint = GlowGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Button(
                    onClick = onRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Allow", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }
}
