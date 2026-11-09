package com.salahkeeper.app.ui.alarm

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.data.model.DuaModel
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.data.service.AlarmScheduler
import com.salahkeeper.app.data.service.PrayerAlarmService
import com.salahkeeper.app.ui.components.AnimatedMeshGradient
import com.salahkeeper.app.ui.components.GlassCard
import com.salahkeeper.app.ui.components.WaveformVisualizer
import com.salahkeeper.app.ui.theme.GlowGreen
import com.salahkeeper.app.ui.theme.SalahKeeperTheme
import com.salahkeeper.app.util.HapticFeedbackUtil
import dagger.hilt.android.AndroidEntryPoint
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AlarmScreenStage {
    ALARM_CALL,
    DUA_RECITE
}

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wakeLockscreen()

        val prayerName = intent.getStringExtra(AlarmScheduler.EXTRA_PRAYER_NAME) ?: "FAJR"
        val prayerType = PrayerType.fromName(prayerName)

        setContent {
            SalahKeeperTheme {
                AlarmScreenContent(
                    prayerType = prayerType,
                    onStopAlarm = {
                        val stopIntent = Intent(this, PrayerAlarmService::class.java).apply {
                            action = PrayerAlarmService.ACTION_STOP_ALARM
                        }
                        startService(stopIntent)
                        finish()
                    },
                    onEmergencyStop = {
                        val emergencyIntent = Intent(this, PrayerAlarmService::class.java).apply {
                            action = PrayerAlarmService.ACTION_EMERGENCY_STOP
                        }
                        startService(emergencyIntent)
                        finish()
                    }
                )
            }
        }
    }

    private fun wakeLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            keyguardManager?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }
}

@Composable
fun AlarmScreenContent(
    prayerType: PrayerType,
    onStopAlarm: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    val context = LocalContext.current
    val hazeState = remember { HazeState() }
    var stage by remember { mutableStateOf(AlarmScreenStage.ALARM_CALL) }
    val duaModel = remember { DuaModel() }

    val recognitionManager = remember {
        DuaRecognitionManager(
            context = context,
            onDuaMatched = {
                HapticFeedbackUtil.performSuccess(context)
            }
        )
    }

    val rmsLevel by recognitionManager.rmsLevel.collectAsState()
    val isMatched by recognitionManager.isMatched.collectAsState()

    DisposableEffect(stage) {
        if (stage == AlarmScreenStage.DUA_RECITE) {
            recognitionManager.startListening()
        }
        onDispose {
            recognitionManager.stopListening()
        }
    }

    // When Dua is matched, trigger completion after a short visual reward
    LaunchedEffect(isMatched) {
        if (isMatched) {
            delay(1500L)
            onStopAlarm()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .haze(hazeState)
    ) {
        // Mesh gradient background
        AnimatedMeshGradient(activePrayer = prayerType)

        if (stage == AlarmScreenStage.ALARM_CALL) {
            AlarmCallStage(
                prayerType = prayerType,
                hazeState = hazeState,
                onImGoingToPray = {
                    HapticFeedbackUtil.performTap(context)
                    stage = AlarmScreenStage.DUA_RECITE
                },
                onEmergencyStop = onEmergencyStop
            )
        } else {
            DuaReciteStage(
                duaModel = duaModel,
                rmsLevel = rmsLevel,
                isMatched = isMatched,
                hazeState = hazeState,
                onManualConfirm = {
                    recognitionManager.manualMatch()
                },
                onEmergencyStop = onEmergencyStop
            )
        }
    }
}

@Composable
fun AlarmCallStage(
    prayerType: PrayerType,
    hazeState: HazeState,
    onImGoingToPray: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "OrbPulse")
    val orbScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "OrbScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Pulsing Glowing Orb & Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(orbScale)
                    .shadow(
                        elevation = 32.dp,
                        shape = CircleShape,
                        ambientColor = prayerType.gradientStart,
                        spotColor = prayerType.gradientEnd
                    )
                    .background(
                        Brush.radialGradient(
                            listOf(
                                prayerType.gradientEnd.copy(alpha = 0.6f),
                                prayerType.gradientStart.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = prayerType.arabicName,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = prayerType.title.uppercase(),
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Time to pause and connect",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom Action Buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Big Glass Button: "I'm going to pray"
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                hazeState = hazeState,
                shape = RoundedCornerShape(24.dp),
                cornerRadius = 24.dp,
                surfaceAlpha = 0.22f,
                borderAlpha = 0.45f,
                onClick = onImGoingToPray
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "I\'m going to pray",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Emergency Stop Button (3-second hold)
            EmergencyStopHoldButton(onTrigger = onEmergencyStop)
        }
    }
}

@Composable
fun DuaReciteStage(
    duaModel: DuaModel,
    rmsLevel: Float,
    isMatched: Boolean,
    hazeState: HazeState,
    onManualConfirm: () -> Unit,
    onEmergencyStop: () -> Unit
) {
    val cardBorderColor by animateColorAsState(
        targetValue = if (isMatched) GlowGreen else Color.White.copy(alpha = 0.3f),
        animationSpec = tween(500),
        label = "DuaBorderColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Center Glass Dua Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(28.dp),
            cornerRadius = 28.dp,
            surfaceAlpha = if (isMatched) 0.26f else 0.16f,
            borderAlpha = if (isMatched) 0.65f else 0.25f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Recite to Stop Alarm",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Arabic Text
                Text(
                    text = duaModel.arabic,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Transliteration
                Text(
                    text = duaModel.transliteration,
                    color = Color(0xFFFDE047),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Translation
                Text(
                    text = "\"${duaModel.translation}\"",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Waveform / Match Status
                if (isMatched) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = "Success",
                            tint = GlowGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Masha\'Allah! May your prayer be accepted.",
                            color = GlowGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    WaveformVisualizer(rmsLevel = rmsLevel, isMatched = false)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Listening for your voice...",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Bottom Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Manual fallback button in case microphone/Google speech is absent
            if (!isMatched) {
                TextButton(onClick = onManualConfirm) {
                    Text(
                        text = "Tap to confirm recitation manually",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.sp
                    )
                }
            }

            // Emergency Stop
            EmergencyStopHoldButton(onTrigger = onEmergencyStop)
        }
    }
}

@Composable
fun EmergencyStopHoldButton(onTrigger: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var holdProgress by remember { mutableFloatStateOf(0f) }
    var holdJob by remember { mutableStateOf<Job?>(null) }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        holdJob?.cancel()
                        holdJob = coroutineScope.launch {
                            val startTime = System.currentTimeMillis()
                            val duration = 3000L // 3 seconds hold requirement
                            while (isActive) {
                                val elapsed = System.currentTimeMillis() - startTime
                                holdProgress = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
                                if (holdProgress >= 1f) {
                                    HapticFeedbackUtil.performSuccess(context)
                                    onTrigger()
                                    break
                                }
                                delay(40L)
                            }
                        }
                        tryAwaitRelease()
                        holdJob?.cancel()
                        holdProgress = 0f
                    }
                )
            }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (holdProgress > 0f) {
                CircularProgressIndicator(
                    progress = { holdProgress },
                    modifier = Modifier.size(16.dp),
                    color = Color(0xFFEF4444),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFCA5A5),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = if (holdProgress > 0f) "Hold to confirm stop..." else "Emergency stop (Hold 3s)",
                color = Color(0xFFFCA5A5),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
