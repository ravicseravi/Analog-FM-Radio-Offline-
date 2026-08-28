package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.FavoriteStation
import com.example.ui.theme.GeometricActivePreset
import com.example.ui.theme.GeometricBorderLight
import com.example.ui.theme.GeometricFavoriteRed
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOnPrimaryContainer
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricPrimaryContainer
import com.example.ui.theme.GeometricSecondaryContainer
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricSurfaceContainerHigh
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import kotlin.math.abs

@Composable
fun TunerControls(
    isPlaying: Boolean,
    isScanning: Boolean,
    isFavorite: Boolean,
    currentFrequency: Float,
    presets: List<FavoriteStation>,
    volume: Float,
    isMuted: Boolean,
    onPlayPauseToggle: () -> Unit,
    onScanNext: () -> Unit,
    onScanPrev: () -> Unit,
    onStepTune: (Float) -> Unit,
    onToggleFavorite: () -> Unit,
    onPresetClick: (FavoriteStation) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GeometricSurface)
            .border(1.dp, GeometricBorderLight, RoundedCornerShape(32.dp))
            .shadow(6.dp, RoundedCornerShape(32.dp))
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Access / Presets Row (Slots 1 to 6)
        Text(
            text = "QUICK ACCESS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = GeometricTextSecondary,
            letterSpacing = 1.8.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val totalPresets = 6
            for (i in 0 until totalPresets) {
                val presetStation = presets.getOrNull(i)
                val isActive = presetStation != null && abs(presetStation.frequency - currentFrequency) < 0.06f

                val slotBg = when {
                    isActive -> GeometricActivePreset
                    presetStation != null -> GeometricSecondaryContainer
                    else -> GeometricSecondaryContainer.copy(alpha = 0.35f)
                }

                val slotBorderColor = when {
                    isActive -> GeometricPrimary
                    presetStation != null -> GeometricPrimary.copy(alpha = 0.3f)
                    else -> GeometricBorderLight
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(slotBg)
                        .border(
                            width = if (isActive) 2.dp else 1.dp,
                            color = slotBorderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = presetStation != null) {
                            presetStation?.let { onPresetClick(it) }
                        }
                        .testTag("preset_button_${i + 1}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CH ${i + 1}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) GeometricOnPrimaryContainer else GeometricTextSecondary
                        )
                        Text(
                            text = presetStation?.formattedFrequency ?: "--",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isActive) GeometricOnPrimaryContainer else if (presetStation != null) GeometricTextPrimary else GeometricTextMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Tuning & Scan Controls: Prev, Central Play/Pause, Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Scan Prev Button (Seek Down)
            FilledTonalButton(
                onClick = onScanPrev,
                enabled = !isScanning,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricPrimaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("scan_prev_button")
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Scan Previous Radio Station",
                    tint = GeometricOnPrimaryContainer
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "SCAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Primary Play / Pause Central Action Button
            Box(
                modifier = Modifier
                    .size(74.dp)
                    .shadow(10.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(GeometricPrimary)
                    .clickable { onPlayPauseToggle() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause Radio" else "Play Radio",
                    tint = GeometricOnPrimary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Scan Next Button (Seek Up)
            FilledTonalButton(
                onClick = onScanNext,
                enabled = !isScanning,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricPrimaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("scan_next_button")
            ) {
                Text(
                    text = "SCAN",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Scan Next Radio Station",
                    tint = GeometricOnPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Geometric Quick Save / Favorite Station Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(GeometricSurfaceContainerHigh)
                .border(1.dp, GeometricBorderLight, RoundedCornerShape(24.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(GeometricPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isFavorite) "Saved in Favorites" else "Save Station",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeometricTextPrimary
                        )
                        Text(
                            text = if (isFavorite) "Tap to remove from quick access" else "Add to your quick access presets",
                            fontSize = 11.sp,
                            color = GeometricTextSecondary
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onToggleFavorite,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isFavorite) GeometricPrimary else GeometricPrimaryContainer,
                        contentColor = if (isFavorite) Color.White else GeometricOnPrimaryContainer
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("favorite_toggle_button")
                ) {
                    Text(
                        text = if (isFavorite) "SAVED" else "SAVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fine Tune Controls (-0.1 and +0.1)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = { onStepTune(-0.1f) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricSecondaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("fine_tune_down")
            ) {
                Icon(
                    imageVector = Icons.Default.FastRewind,
                    contentDescription = "Tune Down 0.1 MHz",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "-0.1 MHz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = { onStepTune(0.1f) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricSecondaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("fine_tune_up")
            ) {
                Text(text = "+0.1 MHz", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.FastForward,
                    contentDescription = "Tune Up 0.1 MHz",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Volume Control Slider Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(GeometricSecondaryContainer.copy(alpha = 0.45f))
                .border(1.dp, GeometricBorderLight, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("mute_toggle_button")
            ) {
                Icon(
                    imageVector = if (isMuted || volume == 0f) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Toggle Mute",
                    tint = if (isMuted) GeometricFavoriteRed else GeometricPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Slider(
                value = if (isMuted) 0f else volume,
                onValueChange = onVolumeChange,
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = GeometricPrimary,
                    activeTrackColor = GeometricPrimary,
                    inactiveTrackColor = GeometricSecondaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .testTag("volume_slider")
            )

            Text(
                text = "${if (isMuted) 0 else (volume * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GeometricTextSecondary,
                modifier = Modifier.width(36.dp)
            )
        }
    }
}

