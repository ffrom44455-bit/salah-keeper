package com.salahkeeper.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.data.model.CalculationMethodOption
import com.salahkeeper.app.data.model.CityPreset
import com.salahkeeper.app.data.model.MadhabOption
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.ui.components.GlassCard
import com.salahkeeper.app.ui.theme.GlowGold
import com.salahkeeper.app.ui.theme.GlowGreen
import com.salahkeeper.app.ui.viewmodel.SettingsViewModel
import com.salahkeeper.app.util.HapticFeedbackUtil
import dev.chrisbanes.haze.HazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    onNavigateToOemGuide: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by settingsViewModel.settings.collectAsState()

    var showCityPicker by remember { mutableStateOf(false) }
    var showManualTimesDialog by remember { mutableStateOf(false) }

    var manualFajr by remember(settings.manualFajrTime) { mutableStateOf(settings.manualFajrTime) }
    var manualDhuhr by remember(settings.manualDhuhrTime) { mutableStateOf(settings.manualDhuhrTime) }
    var manualAsr by remember(settings.manualAsrTime) { mutableStateOf(settings.manualAsrTime) }
    var manualMaghrib by remember(settings.manualMaghribTime) { mutableStateOf(settings.manualMaghribTime) }
    var manualIsha by remember(settings.manualIshaTime) { mutableStateOf(settings.manualIshaTime) }

    var calcDropdownExpanded by remember { mutableStateOf(false) }
    var madhabDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Settings",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure prayer methods, alarms & system reliability",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION: Test Alarm Now
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp,
            surfaceAlpha = 0.18f,
            borderAlpha = 0.4f
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Test Alarm Loop Now",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Fires in 5 seconds to test lockscreen intent and audio loop.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = {
                        HapticFeedbackUtil.performSuccess(context)
                        settingsViewModel.triggerTestAlarm()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = "Test",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("Test", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Pre-Prayer Preparation Reminder (7 mins before)
        Text(
            text = "Pre-Prayer Preparation Reminder",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pre-Prayer Reminder", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Notifies you ${settings.preReminderMinutes} minutes before prayer to make wudu & prepare.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.isPreReminderEnabled,
                        onCheckedChange = { settingsViewModel.setPreReminder(it, settings.preReminderMinutes) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowGreen)
                    )
                }

                if (settings.isPreReminderEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(5, 7, 10, 15).forEach { mins ->
                            val isSelected = settings.preReminderMinutes == mins
                            GlassCard(
                                modifier = Modifier.weight(1f),
                                hazeState = hazeState,
                                shape = RoundedCornerShape(12.dp),
                                cornerRadius = 12.dp,
                                surfaceAlpha = if (isSelected) 0.28f else 0.10f,
                                borderAlpha = if (isSelected) 0.6f else 0.2f,
                                onClick = {
                                    HapticFeedbackUtil.performTap(context)
                                    settingsViewModel.setPreReminder(true, mins)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}m before",
                                        color = if (isSelected) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: In-App Auto Updates
        val updateState by settingsViewModel.updateState.collectAsState()
        Text(
            text = "App Updates & Auto-Upgrade",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current Version: v${settingsViewModel.currentVersion}", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Sideloaded Auto-Updater", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            HapticFeedbackUtil.performTap(context)
                            settingsViewModel.checkForUpdates()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.18f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Check Now", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Update Status Visuals
                when (val state = updateState) {
                    is com.salahkeeper.app.data.service.UpdateState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF38BDF8), strokeWidth = 2.dp)
                            Text("Checking GitHub Releases for new updates...", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        }
                    }
                    is com.salahkeeper.app.data.service.UpdateState.UpToDate -> {
                        Text("✓ You have the latest version installed.", color = GlowGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                    is com.salahkeeper.app.data.service.UpdateState.Available -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🎉 New version available: v${state.versionTag}", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold)
                            Text(state.releaseNotes, color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp)
                            Button(
                                onClick = { settingsViewModel.downloadAndInstall(state.downloadUrl) },
                                colors = ButtonDefaults.buttonColors(containerColor = GlowGreen),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Download & Install Update Now", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    is com.salahkeeper.app.data.service.UpdateState.Downloading -> {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Downloading update: ${state.progressPercent}%", color = Color.White, fontSize = 12.sp)
                            androidx.compose.material3.LinearProgressIndicator(
                                progress = { state.progressPercent / 100f },
                                modifier = Modifier.fillMaxWidth(),
                                color = GlowGreen,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                    is com.salahkeeper.app.data.service.UpdateState.ReadyToInstall -> {
                        Text("Ready to install! Triggering package installer...", color = GlowGreen, fontSize = 12.sp)
                    }
                    is com.salahkeeper.app.data.service.UpdateState.Error -> {
                        Text(state.message, color = Color(0xFFFCA5A5), fontSize = 11.sp)
                    }
                    else -> {}
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Calculation Method & Madhab
        Text(
            text = "Calculation Rules",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Method Picker
                ExposedDropdownMenuBox(
                    expanded = calcDropdownExpanded,
                    onExpandedChange = { calcDropdownExpanded = !calcDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = settings.calculationMethod.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Calculation Method", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = calcDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = calcDropdownExpanded,
                        onDismissRequest = { calcDropdownExpanded = false }
                    ) {
                        CalculationMethodOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.title, fontSize = 13.sp) },
                                onClick = {
                                    settingsViewModel.setCalculationMethod(option)
                                    calcDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Madhab Picker
                ExposedDropdownMenuBox(
                    expanded = madhabDropdownExpanded,
                    onExpandedChange = { madhabDropdownExpanded = !madhabDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = settings.madhab.title,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asr Juristic Method", color = Color.White.copy(alpha = 0.7f)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = madhabDropdownExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = madhabDropdownExpanded,
                        onDismissRequest = { madhabDropdownExpanded = false }
                    ) {
                        MadhabOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.title, fontSize = 13.sp) },
                                onClick = {
                                    settingsViewModel.setMadhab(option)
                                    madhabDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Location & Overrides
        Text(
            text = "Location & Prayer Overrides",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Use Auto GPS Location", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (settings.useGps) "Auto-updates via FusedLocation" else "Manual city / override selected",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.useGps,
                        onCheckedChange = { settingsViewModel.toggleGps(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowGreen)
                    )
                }

                // Manual City Picker button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCityPicker = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.LocationCity, contentDescription = null, tint = Color(0xFF38BDF8))
                        Column {
                            Text("Manual City Picker", color = Color.White, fontWeight = FontWeight.Medium)
                            Text(settings.cityName, color = Color.White.copy(alpha = 0.65f), fontSize = 12.sp)
                        }
                    }
                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }

                // Manual 5 Times Custom Input
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showManualTimesDialog = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = null, tint = GlowGold)
                        Column {
                            Text("Type 5 Prayer Times Manually", color = Color.White, fontWeight = FontWeight.Medium)
                            Text(
                                if (settings.useManualTimes) "Active manual override" else "Disabled (using calculation)",
                                color = if (settings.useManualTimes) GlowGold else Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Per-Prayer Alarm Switches
        Text(
            text = "Prayer Alarms Enabled",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrayerType.entries.forEach { prayer ->
                    val isEnabled = settings.isPrayerEnabled(prayer)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(prayer.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text(prayer.arabicName, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { settingsViewModel.togglePrayer(prayer, it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: OEM Battery Killer Guide & Exemption
        Text(
            text = "Reliability & Background Execution",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp,
            onClick = onNavigateToOemGuide
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BatteryAlert,
                        contentDescription = "OEM Battery",
                        tint = GlowGold,
                        modifier = Modifier.size(28.dp)
                    )
                    Column {
                        Text("OEM Battery Killer Settings", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Xiaomi, Oppo, Samsung instructions", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
                Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Performance & Visuals
        Text(
            text = "Appearance & Performance",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(10.dp))

        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reduce Effects", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Disables real-time blur and intensive animations for low-end devices.",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.reduceEffects,
                        onCheckedChange = { settingsViewModel.setReduceEffects(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = GlowGreen)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dark Mode Glass", color = Color.White, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "Dark translucent aesthetics (recommended).",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = settings.isDarkMode,
                        onCheckedChange = { settingsViewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF38BDF8))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION: Creator Signature
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp,
            surfaceAlpha = 0.12f,
            borderAlpha = 0.25f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✦ SALAH KEEPER ✦",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Crafted with care by",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Zayb",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Dedicated to keeping every believer connected with their Creator",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    // City Picker Dialog
    if (showCityPicker) {
        AlertDialog(
            onDismissRequest = { showCityPicker = false },
            containerColor = Color(0xFF0F172A),
            title = { Text("Select City Preset", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.height(300.dp)) {
                    items(CityPreset.PRESETS) { city ->
                        Text(
                            text = city.displayName,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingsViewModel.selectCityPreset(city)
                                    showCityPicker = false
                                }
                                .padding(vertical = 10.dp, horizontal = 4.dp),
                            fontSize = 14.sp
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCityPicker = false }) {
                    Text("Cancel", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }

    // Manual 5 Times Dialog
    if (showManualTimesDialog) {
        val parsedFajr = com.salahkeeper.app.util.SmartTimeParser.parse(manualFajr, PrayerType.FAJR)
        val parsedDhuhr = com.salahkeeper.app.util.SmartTimeParser.parse(manualDhuhr, PrayerType.DHUHR)
        val parsedAsr = com.salahkeeper.app.util.SmartTimeParser.parse(manualAsr, PrayerType.ASR)
        val parsedMaghrib = com.salahkeeper.app.util.SmartTimeParser.parse(manualMaghrib, PrayerType.MAGHRIB)
        val parsedIsha = com.salahkeeper.app.util.SmartTimeParser.parse(manualIsha, PrayerType.ISHA)

        AlertDialog(
            onDismissRequest = { showManualTimesDialog = false },
            containerColor = Color(0xFF0F172A),
            title = { Text("Manual Prayer Times", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Type any time format (e.g. 5, 5am, 1:30, 4:30, 8:30). The app automatically understands AM & PM for each prayer!",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    OutlinedTextField(
                        value = manualFajr,
                        onValueChange = { manualFajr = it },
                        label = { Text("Fajr Time") },
                        supportingText = {
                            Text("💡 Sets alarm: ${parsedFajr.formatted12h}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = manualDhuhr,
                        onValueChange = { manualDhuhr = it },
                        label = { Text("Dhuhr Time") },
                        supportingText = {
                            Text("💡 Sets alarm: ${parsedDhuhr.formatted12h}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = manualAsr,
                        onValueChange = { manualAsr = it },
                        label = { Text("Asr Time") },
                        supportingText = {
                            Text("💡 Sets alarm: ${parsedAsr.formatted12h}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = manualMaghrib,
                        onValueChange = { manualMaghrib = it },
                        label = { Text("Maghrib Time") },
                        supportingText = {
                            Text("💡 Sets alarm: ${parsedMaghrib.formatted12h}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = manualIsha,
                        onValueChange = { manualIsha = it },
                        label = { Text("Isha Time") },
                        supportingText = {
                            Text("💡 Sets alarm: ${parsedIsha.formatted12h}", color = Color(0xFF38BDF8), fontSize = 11.sp)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateManualTimes(
                            enabled = true,
                            fajr = parsedFajr.formatted12h,
                            dhuhr = parsedDhuhr.formatted12h,
                            asr = parsedAsr.formatted12h,
                            maghrib = parsedMaghrib.formatted12h,
                            isha = parsedIsha.formatted12h
                        )
                        showManualTimesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GlowGold)
                ) {
                    Text("Save & Apply", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.updateManualTimes(false, manualFajr, manualDhuhr, manualAsr, manualMaghrib, manualIsha)
                        showManualTimesDialog = false
                    }
                ) {
                    Text("Disable Override", color = Color(0xFFFCA5A5))
                }
            }
        )
    }
}
