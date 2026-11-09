package com.salahkeeper.app.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.ui.components.GlassCard
import com.salahkeeper.app.ui.components.StreakShareCard
import com.salahkeeper.app.ui.theme.GlowGold
import com.salahkeeper.app.ui.theme.GlowGreen
import com.salahkeeper.app.ui.viewmodel.HistoryViewModel
import com.salahkeeper.app.util.HapticFeedbackUtil
import com.salahkeeper.app.util.ImageShareUtil
import dev.chrisbanes.haze.HazeState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel,
    hazeState: HazeState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentView = LocalView.current
    val stats by historyViewModel.statistics.collectAsState()
    val settings by historyViewModel.settings.collectAsState()

    var showShareDialog by remember { mutableStateOf(false) }
    var creatorNameInput by remember(settings.creatorName) { mutableStateOf(settings.creatorName) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(36.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "History & Streaks",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reflect upon your prayer journey",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }

            // Share Streak Button
            GlassCard(
                hazeState = hazeState,
                shape = CircleShape,
                cornerRadius = 20.dp,
                surfaceAlpha = 0.18f,
                onClick = {
                    HapticFeedbackUtil.performTap(context)
                    showShareDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.IosShare,
                        contentDescription = "Share",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Share Streak",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Two Metric Cards: Weekly & Monthly
        stats?.let { data ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Weekly Score Ring
                GlassCard(
                    modifier = Modifier.weight(1f),
                    hazeState = hazeState,
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Weekly Devotion",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { data.weeklyPercentage / 100f },
                                modifier = Modifier.size(76.dp),
                                color = GlowGreen,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "${data.weeklyPercentage}%",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Last 7 Days",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )
                    }
                }

                // Monthly Score
                GlassCard(
                    modifier = Modifier.weight(1f),
                    hazeState = hazeState,
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Monthly Devotion",
                            color = Color.White.copy(alpha = 0.75f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { data.monthlyPercentage / 100f },
                                modifier = Modifier.size(76.dp),
                                color = Color(0xFF38BDF8),
                                trackColor = Color.White.copy(alpha = 0.1f),
                                strokeWidth = 8.dp
                            )
                            Text(
                                text = "${data.monthlyPercentage}%",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Last 30 Days",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Longest Streak vs Current Streak Row
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = RoundedCornerShape(22.dp),
                cornerRadius = 22.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Current
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = "Current Streak",
                            tint = GlowGold,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "${data.currentStreak} Days",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Current Streak",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(Color.White.copy(alpha = 0.15f))
                    )

                    // Longest
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = "Longest Streak",
                            tint = Color(0xFFFDE047),
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "${data.longestStreak} Days",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Longest Record",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Calendar Heatmap Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                shape = RoundedCornerShape(24.dp),
                cornerRadius = 24.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Prayer Heatmap (Last 4 Weeks)",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Glowing green indicates all daily prayers completed.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CalendarHeatmapGrid(heatmapData = data.calendarHeatmap)
                }
            }
        }

        Spacer(modifier = Modifier.height(96.dp))
    }

    // Share Dialog
    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            containerColor = Color(0xFF0F172A),
            title = {
                Text("Share Your Prayer Streak", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StreakShareCard(
                        streakDays = stats?.currentStreak ?: 0,
                        creatorName = creatorNameInput
                    )

                    OutlinedTextField(
                        value = creatorNameInput,
                        onValueChange = {
                            creatorNameInput = it
                            historyViewModel.updateCreatorName(it)
                        },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareDialog = false
                        // Generate simple styled bitmap
                        val bitmap = Bitmap.createBitmap(800, 1200, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.parseColor("#090D16"))
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 48f
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        canvas.drawText("SALAH KEEPER", 400f, 250f, paint)
                        paint.textSize = 120f
                        paint.color = android.graphics.Color.parseColor("#F59E0B")
                        val streakText = "${stats?.currentStreak ?: 0}"
                        canvas.drawText(streakText, 400f, 480f, paint)
                        paint.textSize = 42f
                        paint.color = android.graphics.Color.WHITE
                        canvas.drawText("DAYS IN A ROW", 400f, 570f, paint)
                        paint.textSize = 28f
                        paint.color = android.graphics.Color.parseColor("#94A3B8")
                        canvas.drawText("Made with Salah Keeper • by $creatorNameInput", 400f, 1050f, paint)

                        ImageShareUtil.shareBitmap(context, bitmap, "I'm on a ${stats?.currentStreak ?: 0} day prayer streak with Salah Keeper! 🤲")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Share Image", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Close", color = Color.White.copy(alpha = 0.7f))
                }
            }
        )
    }
}

@Composable
fun CalendarHeatmapGrid(heatmapData: Map<String, Float>) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()

    // 4 weeks: 28 days
    val dates = (0 until 28).map { offset ->
        val c = Calendar.getInstance().apply {
            time = cal.time
            add(Calendar.DAY_OF_YEAR, -27 + offset)
        }
        dateFormat.format(c.time)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (week in 0 until 4) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (day in 0 until 7) {
                    val index = week * 7 + day
                    val dateStr = dates[index]
                    val ratio = heatmapData[dateStr] ?: 0f

                    val cellColor = when {
                        ratio >= 0.8f -> GlowGreen
                        ratio >= 0.4f -> GlowGold
                        ratio > 0f -> Color(0xFF38BDF8)
                        else -> Color.White.copy(alpha = 0.08f)
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(cellColor)
                            .border(
                                1.dp,
                                if (ratio > 0f) cellColor.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.05f),
                                RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }
}
