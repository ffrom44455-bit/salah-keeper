package com.salahkeeper.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.salahkeeper.app.data.model.PrayerType
import com.salahkeeper.app.ui.theme.LocalReduceEffects
import com.salahkeeper.app.ui.theme.MidnightDeep

@Composable
fun AnimatedMeshGradient(
    activePrayer: PrayerType,
    modifier: Modifier = Modifier
) {
    val reduceEffects = LocalReduceEffects.current

    val animStartColor by animateColorAsState(
        targetValue = activePrayer.gradientStart,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "StartColorAnim"
    )

    val animEndColor by animateColorAsState(
        targetValue = activePrayer.gradientEnd,
        animationSpec = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
        label = "EndColorAnim"
    )

    if (reduceEffects) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(MidnightDeep, animStartColor.copy(alpha = 0.5f), MidnightDeep)
                    )
                )
        )
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "MeshMotion")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "MeshPhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Deep night baseline
        drawRect(color = MidnightDeep)

        // Orb 1: Upper right moving down
        val orb1Center = Offset(
            x = width * (0.8f - phase * 0.15f),
            y = height * (0.15f + phase * 0.15f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animStartColor.copy(alpha = 0.55f),
                    animStartColor.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = orb1Center,
                radius = width * 0.9f
            ),
            center = orb1Center,
            radius = width * 0.9f
        )

        // Orb 2: Lower left moving up
        val orb2Center = Offset(
            x = width * (0.2f + phase * 0.2f),
            y = height * (0.75f - phase * 0.2f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animEndColor.copy(alpha = 0.5f),
                    animEndColor.copy(alpha = 0.12f),
                    Color.Transparent
                ),
                center = orb2Center,
                radius = width * 0.85f
            ),
            center = orb2Center,
            radius = width * 0.85f
        )

        // Center soft glow
        val orb3Center = Offset(x = width * 0.5f, y = height * 0.45f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    animEndColor.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                center = orb3Center,
                radius = width * 0.65f
            ),
            center = orb3Center,
            radius = width * 0.65f
        )
    }
}
