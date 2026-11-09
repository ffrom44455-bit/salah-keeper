package com.salahkeeper.app.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.salahkeeper.app.ui.theme.LocalReduceEffects
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = RoundedCornerShape(24.dp),
    cornerRadius: Dp = 24.dp,
    elevation: Dp = 8.dp,
    borderAlpha: Float = 0.22f,
    surfaceAlpha: Float = 0.14f,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val reduceEffects = LocalReduceEffects.current
    val isBlurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !reduceEffects

    val actualSurfaceAlpha = if (isBlurSupported) surfaceAlpha else 0.28f

    val outerModifier = modifier
        .shadow(
            elevation = if (reduceEffects) 2.dp else elevation,
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.35f),
            spotColor = Color.Black.copy(alpha = 0.35f)
        )
        .clip(shape)
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )

    Box(modifier = outerModifier) {
        // Layer 1: Backdrop blur (hazeChild) + glass gradient fill + frosted border
        // By isolating hazeChild to a matchParentSize layer, the content is never blurred or obscured!
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (isBlurSupported && hazeState != null) {
                        Modifier.hazeChild(
                            state = hazeState,
                            shape = shape,
                            style = HazeDefaults.style(
                                backgroundColor = Color(0xFF0F172A).copy(alpha = 0.25f),
                                blurRadius = 20.dp,
                                noiseFactor = 0.03f
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = actualSurfaceAlpha + 0.05f),
                            Color.White.copy(alpha = actualSurfaceAlpha * 0.7f)
                        )
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = borderAlpha),
                                Color.White.copy(alpha = borderAlpha * 0.35f)
                            )
                        )
                    ),
                    shape = shape
                )
        )

        // Layer 2: Card Content - always crisp, sharp, and 100% readable
        content()
    }
}
