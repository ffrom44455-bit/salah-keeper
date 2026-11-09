package com.salahkeeper.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salahkeeper.app.ui.theme.GlowGold
import com.salahkeeper.app.ui.theme.MidnightDeep

@Composable
fun StreakShareCard(
    streakDays: Int,
    creatorName: String,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(32.dp)

    Box(
        modifier = modifier
            .width(320.dp)
            .clip(cardShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        MidnightDeep,
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                1.5.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.35f),
                        Color.White.copy(alpha = 0.08f)
                    )
                ),
                cardShape
            )
            .padding(28.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding Pill
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "✦ SALAH KEEPER ✦",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Glowing Flame Circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(elevation = 20.dp, shape = CircleShape, ambientColor = GlowGold, spotColor = GlowGold)
                    .background(
                        Brush.radialGradient(
                            listOf(GlowGold.copy(alpha = 0.35f), Color.Transparent)
                        ),
                        CircleShape
                    )
                    .border(1.dp, GlowGold.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocalFireDepartment,
                    contentDescription = "Streak Flame",
                    tint = GlowGold,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Streak Count
            Text(
                text = "$streakDays",
                color = Color.White,
                fontSize = 58.sp,
                fontWeight = FontWeight.Black,
                lineHeight = 60.sp
            )

            Text(
                text = if (streakDays == 1) "DAY STREAK" else "DAYS IN A ROW",
                color = Color(0xFFFDE047),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Inspiring text
            Text(
                text = "\"Indeed, prayer has been decreed upon the believers a decree of specified times.\"",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Surah An-Nisa 4:103",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(26.dp))

            // Footer Signature
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Made with Salah Keeper • by $creatorName",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
