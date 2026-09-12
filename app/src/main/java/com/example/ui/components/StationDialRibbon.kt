package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.ui.theme.GeometricBorderLight
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOnPrimaryContainer
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricPrimaryContainer
import com.example.ui.theme.GeometricSecondaryContainer
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import java.util.Locale
import kotlin.math.abs

/**
 * Interactive horizontal FM band station strip.
 * Bridges the hero display and the playback controls, allowing instant 1-tap tuning.
 */
@Composable
fun StationDialRibbon(
    stations: List<RadioStation>,
    currentFrequency: Float,
    isPlaying: Boolean,
    onStationSelect: (RadioStation) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to selected station
    LaunchedEffect(currentFrequency) {
        val selectedIndex = stations.indexOfFirst { abs(it.frequency - currentFrequency) < 0.1f }
        if (selectedIndex >= 0) {
            val targetScroll = (selectedIndex * 110).coerceAtLeast(0)
            scrollState.animateScrollTo(targetScroll)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FM STATIONS SPECTRUM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GeometricTextSecondary,
                letterSpacing = 1.4.sp
            )

            Text(
                text = "${stations.size} Stations Live",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeometricPrimary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stations.forEach { station ->
                val isSelected = abs(station.frequency - currentFrequency) < 0.1f

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) GeometricPrimary else GeometricSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GeometricPrimary else GeometricBorderLight,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onStationSelect(station) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("station_chip_${String.format(Locale.US, "%.1f", station.frequency)}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isPlaying) Color(0xFF4CAF50) else Color.White)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = String.format(Locale.US, "%.1f", station.frequency),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) GeometricOnPrimary else GeometricPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = station.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) GeometricOnPrimary.copy(alpha = 0.9f) else GeometricTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
