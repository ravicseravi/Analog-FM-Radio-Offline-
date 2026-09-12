package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.ui.theme.GeometricBorderLight
import com.example.ui.theme.GeometricOnPrimaryContainer
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricPrimaryContainer
import com.example.ui.theme.GeometricSecondaryContainer
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import java.util.Locale

@Composable
fun TunerDisplay(
    frequency: Float,
    station: RadioStation?,
    isPlaying: Boolean,
    isScanning: Boolean,
    signalStrength: Int,
    rdsText: String,
    equalizerBars: List<Float>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_pulse")
    val scanAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.dp,
                color = GeometricBorderLight,
                shape = RoundedCornerShape(26.dp)
            )
            .shadow(4.dp, RoundedCornerShape(26.dp)),
        color = GeometricSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            // Header Row: Broadcast Badge, Stereo pill, and Signal Quality Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FM Badge & Stereo indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GeometricPrimaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FM INDIA",
                            color = GeometricOnPrimaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.0.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(GeometricSecondaryContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isPlaying) Color(0xFF2E7D32) else GeometricTextSecondary)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "STEREO",
                                color = GeometricTextPrimary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Signal Quality Indicator (4-bar visual meter)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    SignalBarsIndicator(signalStrength = if (isScanning) 50 else signalStrength)
                    Text(
                        text = if (isScanning) "SCANNING…" else "$signalStrength% Signal",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (signalStrength > 70) Color(0xFF2E7D32) else GeometricTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Station Info & Large Frequency Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Station Name & City/Genre
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = station?.name ?: "Indian FM Radio",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = GeometricTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.2.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = station?.let { "${it.city} • ${it.genre}" } ?: "87.5 - 108.0 MHz FM Band",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = GeometricTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Big Digital Frequency Display with MHz unit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(GeometricSecondaryContainer.copy(alpha = 0.6f))
                        .border(1.dp, GeometricBorderLight, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("frequency_display"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = String.format(Locale.US, "%.1f", frequency),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isScanning) GeometricPrimary.copy(alpha = scanAlpha) else GeometricPrimary,
                            letterSpacing = (-1.0).sp,
                            lineHeight = 34.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "MHz",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeometricTextSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live RDS Track Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GeometricSecondaryContainer.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = GeometricPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isScanning) "Sweeping Indian FM frequencies for strongest live broadcast…" else rdsText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeometricTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Audio Equalizer Spectrum Bars (Sleek Visualizer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GeometricSecondaryContainer.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                equalizerBars.forEachIndexed { index, heightFraction ->
                    val barHeight = if (isPlaying) (heightFraction * 18).coerceAtLeast(3f).dp else 3.dp
                    val barColor = if (isPlaying) {
                        when (index % 3) {
                            0 -> GeometricPrimary
                            1 -> GeometricPrimary.copy(alpha = 0.85f)
                            else -> GeometricOnPrimaryContainer
                        }
                    } else {
                        GeometricTextMuted
                    }

                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .background(barColor)
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalBarsIndicator(signalStrength: Int) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(14.dp)
    ) {
        val activeColor = if (signalStrength > 70) Color(0xFF2E7D32) else GeometricPrimary
        val inactiveColor = GeometricTextMuted.copy(alpha = 0.4f)

        val activeBars = when {
            signalStrength >= 80 -> 4
            signalStrength >= 50 -> 3
            signalStrength >= 25 -> 2
            else -> 1
        }

        listOf(4.dp, 7.dp, 10.dp, 13.dp).forEachIndexed { index, height ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(height)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (index < activeBars) activeColor else inactiveColor)
            )
        }
    }
}
