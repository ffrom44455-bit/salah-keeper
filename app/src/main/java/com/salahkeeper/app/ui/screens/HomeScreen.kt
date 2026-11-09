package com.salahkeeper.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.data.model.PrayerStatus
import com.salahkeeper.app.data.model.PrayerTiming
import com.salahkeeper.app.ui.components.CircularProgressCountdown
import com.salahkeeper.app.ui.components.GlassCard
import com.salahkeeper.app.ui.components.PrayerTimeChip
import com.salahkeeper.app.ui.theme.GlowGold
import com.salahkeeper.app.ui.theme.GlowGreen
import com.salahkeeper.app.ui.theme.GlowRed
import com.salahkeeper.app.ui.viewmodel.HomeViewModel
import com.salahkeeper.app.ui.viewmodel.SettingsViewModel
import com.salahkeeper.app.util.HapticFeedbackUtil
import dev.chrisbanes.haze.HazeState

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val overview by homeViewModel.todayOverview.collectAsState()
    val settings by settingsViewModel.settingsFlow.collectAsState(initial = settingsViewModel.settings.value)

    var selectedTimingForEdit by remember { mutableStateOf<PrayerTiming?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Top App Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Salah Keeper",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Time to pause and connect",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            // Location Pill
            GlassCard(
                hazeState = hazeState,
                shape = CircleShape,
                cornerRadius = 20.dp,
                surfaceAlpha = 0.16f,
                onClick = {
                    HapticFeedbackUtil.performTap(context)
                    homeViewModel.refreshLocation()
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = settings.cityName.take(16),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        val updateState by settingsViewModel.updateState.collectAsState()
        if (updateState is com.salahkeeper.app.data.service.UpdateState.Available) {
            val update = updateState as com.salahkeeper.app.data.service.UpdateState.Available
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                hazeState = hazeState,
                shape = RoundedCornerShape(16.dp),
                cornerRadius = 16.dp,
                surfaceAlpha = 0.22f,
                borderAlpha = 0.5f,
                onClick = { settingsViewModel.downloadAndInstall(update.downloadUrl) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🚀 New Update Available (v${update.versionTag})", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Tap to download and install automatically.", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                    }
                    Text("Update", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Hero Card with Countdown
        overview?.let { data ->
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = RoundedCornerShape(28.dp),
                cornerRadius = 28.dp,
                elevation = 16.dp,
                surfaceAlpha = 0.16f
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressCountdown(
                        prayerTiming = data.nextPrayer,
                        remainingMillis = data.remainingMillisToNext,
                        size = 230.dp,
                        strokeWidth = 11.dp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scheduled at ${data.nextPrayer.formattedTime}",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Today's 5 Prayers Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today\'s Prayers",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap to update status",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Row of 5 Glass Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(data.timings) { timing ->
                    PrayerTimeChip(
                        timing = timing,
                        isCurrentNext = timing.type == data.nextPrayer.type,
                        hazeState = hazeState,
                        onClick = {
                            HapticFeedbackUtil.performTap(context)
                            selectedTimingForEdit = timing
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Streak & Encouragement Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = RoundedCornerShape(22.dp),
                cornerRadius = 22.dp,
                surfaceAlpha = 0.14f
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(GlowGold.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = GlowGold,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (data.currentStreak > 0) "${data.currentStreak} Day Streak" else "Start Your Streak",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = data.encouragingMessage,
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp)) // padding for bottom nav
    }

    // Quick Status Editor Dialog
    selectedTimingForEdit?.let { timing ->
        AlertDialog(
            onDismissRequest = { selectedTimingForEdit = null },
            containerColor = Color(0xFF1E293B),
            title = {
                Text(
                    text = "Update ${timing.type.title} Status",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose your prayer log status for today:",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = {
                            homeViewModel.updatePrayerStatus(timing, PrayerStatus.ON_TIME)
                            selectedTimingForEdit = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = GlowGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✓ Prayed On Time")
                    }
                    TextButton(
                        onClick = {
                            homeViewModel.updatePrayerStatus(timing, PrayerStatus.LATE)
                            selectedTimingForEdit = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = GlowGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⏳ Prayed Late")
                    }
                    TextButton(
                        onClick = {
                            homeViewModel.updatePrayerStatus(timing, PrayerStatus.MISSED)
                            selectedTimingForEdit = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = GlowRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✕ Missed (Send to Qaza)")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedTimingForEdit = null }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}
