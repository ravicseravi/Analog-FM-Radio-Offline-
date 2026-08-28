package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Sensors
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.ui.theme.GeometricActivePreset
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

private const val MIN_FREQ = 87.5f
private const val MAX_FREQ = 108.0f
private const val FREQ_RANGE = MAX_FREQ - MIN_FREQ // 20.5 MHz

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
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanAlpha"
    )

    val scanRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanRotation"
    )

    // Animated frequency fraction along the 87.5 - 108.0 MHz range
    val currentFraction = ((frequency - MIN_FREQ) / FREQ_RANGE).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(
        targetValue = currentFraction,
        animationSpec = tween(durationMillis = 140),
        label = "gauge_arc"
    )

    // Geometric Balance Main Surface Card
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .border(
                width = 1.dp,
                color = GeometricBorderLight,
                shape = RoundedCornerShape(32.dp)
            )
            .shadow(6.dp, RoundedCornerShape(32.dp)),
        color = GeometricSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status Bar: FM Broadcast Pill & Frequency Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GeometricPrimaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "FM BROADCAST (INDIA)",
                        color = GeometricOnPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isScanning) "SCANNING…" else "87.5 – 108.0 MHz",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isScanning) GeometricPrimary else GeometricTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Iconic Geometric Balance Circular Gauge Centerpiece
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 12.dp.toPx()
                    val arcPadding = strokeWidthPx / 2f
                    val arcSize = size.width - (arcPadding * 2f)

                    // Background complete concentric balance ring
                    drawArc(
                        color = Color(0xFFE8DEF8).copy(alpha = 0.6f),
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = androidx.compose.ui.geometry.Offset(arcPadding, arcPadding),
                        size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    // Active Iris arc tracking frequency or rotating when scanning
                    if (isScanning) {
                        drawArc(
                            color = GeometricPrimary,
                            startAngle = scanRotation,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(arcPadding, arcPadding),
                            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    } else {
                        val activeSweep = (animatedFraction * 360f).coerceIn(12f, 360f)
                        drawArc(
                            color = GeometricPrimary,
                            startAngle = -90f,
                            sweepAngle = activeSweep,
                            useCenter = false,
                            topLeft = androidx.compose.ui.geometry.Offset(arcPadding, arcPadding),
                            size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                        )
                    }
                }

                // Centered Digital Frequency & Stereo Pill
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "FREQUENCY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.5.sp,
                        color = GeometricTextSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.testTag("frequency_display")
                    ) {
                        Text(
                            text = String.format(Locale.US, "%.1f", frequency),
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Light,
                            color = if (isScanning) GeometricPrimary.copy(alpha = scanAlpha) else GeometricTextPrimary,
                            letterSpacing = (-1.5).sp,
                            lineHeight = 54.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "MHz",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeometricTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Geometric Stereo Indicator Pill
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(GeometricSecondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (station != null && isPlaying) GeometricPrimary else GeometricTextMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (station != null) "STEREO" else "MONO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = if (station != null && isPlaying) GeometricPrimary else GeometricTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Station Name & Genre Metadata
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = station?.name ?: if (isScanning) "Searching Available Stations…" else "Tuning / Static Frequency",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeometricTextPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (station != null) {
                        if (rdsText.startsWith("Now Playing:") || rdsText.contains("•")) rdsText else "Now Playing: $rdsText"
                    } else {
                        "Indian FM Spectrum (87.5 – 108.0 MHz) • Scan or tune frequency"
                    },
                    fontSize = 13.sp,
                    color = GeometricTextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Balanced Row: Signal Quality Badge & Real-time Equalizer Visualizer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal Quality Meter
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(GeometricSecondaryContainer)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = "Signal Strength",
                        tint = GeometricPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${if (signalStrength > 75) "Strong Signal" else if (signalStrength > 45) "Good Signal" else "Tuning"} ($signalStrength%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeometricPrimary
                    )
                }

                // Geometric Spectrum Equalizer Bars
                Row(
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GeometricSecondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    equalizerBars.forEachIndexed { index, level ->
                        val targetHeight = if (isPlaying && !isScanning) (level * 22f).coerceIn(3f, 22f).dp else 3.dp
                        val animatedHeight by animateFloatAsState(
                            targetValue = targetHeight.value,
                            animationSpec = tween(80),
                            label = "eq_bar_$index"
                        )
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(animatedHeight.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(if (index % 2 == 0) GeometricPrimary else GeometricActivePreset)
                        )
                    }
                }
            }
        }
    }
}

