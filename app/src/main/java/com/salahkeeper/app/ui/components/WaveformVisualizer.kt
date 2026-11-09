package com.salahkeeper.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.salahkeeper.app.ui.theme.GlowGreen
import kotlin.math.sin

@Composable
fun WaveformVisualizer(
    rmsLevel: Float,
    isMatched: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 14
) {
    val barColor by animateColorAsState(
        targetValue = if (isMatched) GlowGreen else Color(0xFF38BDF8),
        animationSpec = tween(durationMillis = 400),
        label = "WaveformColor"
    )

    Row(
        modifier = modifier.height(60.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = 0.4f + 0.6f * sin((i.toDouble() / barCount) * Math.PI).toFloat()
            val targetHeight = if (isMatched) {
                36.dp
            } else {
                val variable = (rmsLevel * 45f * factor).coerceIn(8f, 52f)
                variable.dp
            }

            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight.value,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                label = "BarHeight_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor.copy(alpha = if (isMatched) 0.95f else 0.8f))
            )
        }
    }
}
