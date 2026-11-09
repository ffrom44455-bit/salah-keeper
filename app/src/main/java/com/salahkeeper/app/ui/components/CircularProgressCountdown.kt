package com.salahkeeper.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.data.model.PrayerTiming

@Composable
fun CircularProgressCountdown(
    prayerTiming: PrayerTiming,
    remainingMillis: Long,
    totalWindowMillis: Long = 4 * 60 * 60 * 1000L, // 4 hours window reference
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 10.dp
) {
    val progress = (1f - (remainingMillis.toFloat() / totalWindowMillis.toFloat())).coerceIn(0.05f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    val hours = (remainingMillis / (1000 * 60 * 60)).coerceAtLeast(0)
    val minutes = ((remainingMillis / (1000 * 60)) % 60).coerceAtLeast(0)
    val seconds = ((remainingMillis / 1000) % 60).coerceAtLeast(0)
    val countdownString = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2f, stroke / 2f)

            // Background Track
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // Outer subtle glow
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        prayerTiming.type.gradientStart.copy(alpha = 0.3f),
                        prayerTiming.type.gradientEnd.copy(alpha = 0.5f),
                        prayerTiming.type.gradientStart.copy(alpha = 0.3f)
                    )
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke * 1.5f, cap = StrokeCap.Round)
            )

            // Active Glowing Foreground Arc
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        prayerTiming.type.gradientStart,
                        prayerTiming.type.gradientEnd,
                        prayerTiming.type.gradientStart
                    )
                ),
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = prayerTiming.type.arabicName,
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = countdownString,
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "until ${prayerTiming.type.title}",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
