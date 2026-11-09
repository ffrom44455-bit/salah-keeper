package com.salahkeeper.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
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
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.ui.components.GlassCard
import com.salahkeeper.app.ui.theme.GlowGold
import com.salahkeeper.app.ui.theme.GlowGreen
import dev.chrisbanes.haze.HazeState

@Composable
fun OemBatteryScreen(
    onBack: () -> Unit,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    val isIgnoringBatteryOptimizations = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "OEM Battery Optimization",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning Hero Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp,
            surfaceAlpha = 0.18f
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = null,
                    tint = GlowGold,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "Why Prayer Alarms Might Be Silenced",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Many device manufacturers (Xiaomi, Samsung, Oppo, Vivo) aggressively kill background processes to save battery, which can kill prayer alarms while your phone is asleep.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Android System Battery Exemption Button
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            hazeState = hazeState,
            shape = RoundedCornerShape(20.dp),
            cornerRadius = 20.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Standard Android Exemption",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isIgnoringBatteryOptimizations) "Exempted (Active)" else "Exemption recommended",
                            color = if (isIgnoringBatteryOptimizations) GlowGreen else GlowGold,
                            fontSize = 12.sp
                        )
                    }

                    if (isIgnoringBatteryOptimizations) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = GlowGreen
                        )
                    }
                }

                Button(
                    onClick = {
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BatteryChargingFull,
                        contentDescription = null,
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Request Battery Exemption",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Manufacturer Step-by-Step Guides",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Xiaomi / HyperOS / MIUI
        OemGuideItem(
            brand = "Xiaomi / Redmi / POCO (HyperOS / MIUI)",
            instructions = listOf(
                "Open Settings > Apps > Manage Apps > Salah Keeper.",
                "Turn ON 'Autostart'.",
                "Open 'Battery Saver' and select 'No restrictions'.",
                "Open recent apps screen, long-press Salah Keeper and tap the Lock icon."
            ),
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Samsung (One UI)
        OemGuideItem(
            brand = "Samsung (One UI)",
            instructions = listOf(
                "Open Settings > Apps > Salah Keeper > Battery.",
                "Select 'Unrestricted'.",
                "Open Settings > Battery and device care > Battery > Background usage limits.",
                "Add Salah Keeper to 'Never sleeping apps'."
            ),
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Oppo / OnePlus / Realme
        OemGuideItem(
            brand = "Oppo / OnePlus / Realme (ColorOS / OxygenOS)",
            instructions = listOf(
                "Open Settings > Apps > Salah Keeper > Battery usage.",
                "Enable 'Allow background activity' and 'Allow auto-launch'.",
                "Lock the app in the recent apps menu."
            ),
            hazeState = hazeState
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Rounded.OpenInNew, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Open App System Settings", color = Color.White)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun OemGuideItem(
    brand: String,
    instructions: List<String>,
    hazeState: HazeState? = null
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        hazeState = hazeState,
        shape = RoundedCornerShape(18.dp),
        cornerRadius = 18.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = brand,
                color = Color(0xFF38BDF8),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            instructions.forEachIndexed { index, step ->
                Text(
                    text = "${index + 1}. $step",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}
