package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.ui.theme.GeometricBorderLight
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricSecondaryContainer
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import kotlin.math.roundToInt

private const val MIN_FREQ = 87.5f
private const val MAX_FREQ = 108.0f
private const val FREQ_RANGE = MAX_FREQ - MIN_FREQ // 20.5 MHz

@Composable
fun FrequencyDial(
    currentFrequency: Float,
    availableStations: List<RadioStation>,
    onFrequencyChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    // Smoothly animate needle when stepping or scanning
    val animatedFreq by animateFloatAsState(
        targetValue = currentFrequency,
        animationSpec = tween(durationMillis = 120),
        label = "needle_freq"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GeometricSurface)
            .border(1.dp, GeometricBorderLight, RoundedCornerShape(24.dp))
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        // Dial Sub-header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ANALOG TUNER DIAL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GeometricTextSecondary,
                letterSpacing = 1.6.sp
            )
            Text(
                text = "DRAG OR TAP TO TUNE",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeometricPrimary,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GeometricSecondaryContainer.copy(alpha = 0.45f))
                .border(1.dp, GeometricBorderLight, RoundedCornerShape(16.dp))
                .testTag("frequency_dial")
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val padding = 28f
                        val dialWidth = size.width - (padding * 2)
                        val fraction = ((offset.x - padding) / dialWidth).coerceIn(0f, 1f)
                        val targetFreq = MIN_FREQ + (fraction * FREQ_RANGE)
                        val rounded = (targetFreq * 10f).roundToInt() / 10f
                        onFrequencyChange(rounded)
                    }
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val padding = 28f
                        val dialWidth = size.width - (padding * 2)
                        val fraction = ((change.position.x - padding) / dialWidth).coerceIn(0f, 1f)
                        val targetFreq = MIN_FREQ + (fraction * FREQ_RANGE)
                        val rounded = (targetFreq * 10f).roundToInt() / 10f
                        onFrequencyChange(rounded)
                    }
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val padding = 28f
                val dialWidth = size.width - (padding * 2)
                val dialHeight = size.height

                // Draw background horizontal center guideline
                drawLine(
                    color = Color(0xFFE8DEF8),
                    start = Offset(padding, dialHeight * 0.5f),
                    end = Offset(size.width - padding, dialHeight * 0.5f),
                    strokeWidth = 1.5f
                )

                // Draw Station Presence Pips (subtle dots where stations exist)
                for (station in availableStations) {
                    val stFraction = ((station.frequency - MIN_FREQ) / FREQ_RANGE).coerceIn(0f, 1f)
                    val stX = padding + (stFraction * dialWidth)
                    drawCircle(
                        color = GeometricPrimary.copy(alpha = 0.55f),
                        radius = 3.5f,
                        center = Offset(stX, dialHeight * 0.5f)
                    )
                }

                // Draw Frequency scale ticks & numbers
                val majorLabels = setOf(88, 92, 96, 100, 104, 108)

                var freq = 88.0f
                while (freq <= 108.0f) {
                    val fraction = ((freq - MIN_FREQ) / FREQ_RANGE).coerceIn(0f, 1f)
                    val x = padding + (fraction * dialWidth)
                    val isInteger = (freq % 1.0f < 0.01f || freq % 1.0f > 0.99f)
                    val intVal = freq.roundToInt()

                    if (isInteger && majorLabels.contains(intVal)) {
                        // Major tick
                        drawLine(
                            color = GeometricPrimary,
                            start = Offset(x, 4f),
                            end = Offset(x, 18f),
                            strokeWidth = 2.2f
                        )
                        drawLine(
                            color = GeometricPrimary,
                            start = Offset(x, dialHeight - 18f),
                            end = Offset(x, dialHeight - 4f),
                            strokeWidth = 2.2f
                        )

                        // Draw frequency number label
                        val text = "$intVal"
                        val textResult = textMeasurer.measure(
                            text = text,
                            style = TextStyle(
                                color = GeometricTextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        )
                        drawText(
                            textLayoutResult = textResult,
                            topLeft = Offset(
                                x - (textResult.size.width / 2),
                                (dialHeight / 2) - (textResult.size.height / 2)
                            )
                        )
                    } else if (isInteger) {
                        // Medium tick
                        drawLine(
                            color = GeometricPrimary.copy(alpha = 0.4f),
                            start = Offset(x, 6f),
                            end = Offset(x, 16f),
                            strokeWidth = 1.6f
                        )
                        drawLine(
                            color = GeometricPrimary.copy(alpha = 0.4f),
                            start = Offset(x, dialHeight - 16f),
                            end = Offset(x, dialHeight - 6f),
                            strokeWidth = 1.6f
                        )
                    } else {
                        // Minor tick every 0.2
                        drawLine(
                            color = Color(0xFFCAC4D0),
                            start = Offset(x, 8f),
                            end = Offset(x, 13f),
                            strokeWidth = 1.0f
                        )
                        drawLine(
                            color = Color(0xFFCAC4D0),
                            start = Offset(x, dialHeight - 13f),
                            end = Offset(x, dialHeight - 8f),
                            strokeWidth = 1.0f
                        )
                    }

                    freq += 0.2f
                }

                // Draw Current Frequency Needle
                val needleFraction = ((animatedFreq - MIN_FREQ) / FREQ_RANGE).coerceIn(0f, 1f)
                val needleX = padding + (needleFraction * dialWidth)

                // Needle glowing shadow/aura
                drawLine(
                    color = GeometricPrimary.copy(alpha = 0.25f),
                    start = Offset(needleX, 0f),
                    end = Offset(needleX, dialHeight),
                    strokeWidth = 6f
                )

                // Main needle line
                drawLine(
                    color = GeometricPrimary,
                    start = Offset(needleX, 0f),
                    end = Offset(needleX, dialHeight),
                    strokeWidth = 2.5f
                )

                // Top indicator pointer triangle
                val topPointer = Path().apply {
                    moveTo(needleX - 5f, 0f)
                    lineTo(needleX + 5f, 0f)
                    lineTo(needleX, 8f)
                    close()
                }
                drawPath(topPointer, color = GeometricPrimary)

                // Bottom indicator pointer triangle
                val bottomPointer = Path().apply {
                    moveTo(needleX - 5f, dialHeight)
                    lineTo(needleX + 5f, dialHeight)
                    lineTo(needleX, dialHeight - 8f)
                    close()
                }
                drawPath(bottomPointer, color = GeometricPrimary)
            }
        }
    }
}

