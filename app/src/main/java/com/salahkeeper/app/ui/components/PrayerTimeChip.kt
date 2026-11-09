package com.salahkeeper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.data.model.PrayerTiming
import dev.chrisbanes.haze.HazeState

@Composable
fun PrayerTimeChip(
    timing: PrayerTiming,
    isCurrentNext: Boolean,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null
) {
    val dotColor = timing.status.dotColor
    val chipShape = RoundedCornerShape(18.dp)

    GlassCard(
        modifier = modifier
            .width(82.dp)
            .height(98.dp),
        hazeState = hazeState,
        shape = chipShape,
        cornerRadius = 18.dp,
        borderAlpha = if (isCurrentNext) 0.55f else 0.2f,
        surfaceAlpha = if (isCurrentNext) 0.22f else 0.12f,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glowing Status Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .shadow(elevation = 6.dp, shape = CircleShape, ambientColor = dotColor, spotColor = dotColor)
                    .background(dotColor, CircleShape)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = timing.type.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = if (isCurrentNext) FontWeight.Bold else FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = timing.formattedTime,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
