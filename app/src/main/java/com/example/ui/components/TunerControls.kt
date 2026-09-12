package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
import com.example.ui.theme.GeometricBorderLight
import com.example.ui.theme.GeometricFavoriteRed
import com.example.ui.theme.GeometricOnPrimary
import com.example.ui.theme.GeometricOnPrimaryContainer
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricPrimaryContainer
import com.example.ui.theme.GeometricSecondaryContainer
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary

/**
 * Modern, tactile Station Control & Playback Deck.
 * High-contrast, responsive, non-scrolling player deck.
 */
@Composable
fun TunerControls(
    isPlaying: Boolean,
    isScanning: Boolean,
    isFavorite: Boolean,
    currentStation: RadioStation?,
    currentFrequency: Float,
    volume: Float,
    isMuted: Boolean,
    onPlayPauseToggle: () -> Unit,
    onPrevStation: () -> Unit,
    onNextStation: () -> Unit,
    onAutoSearch: () -> Unit,
    onToggleFavorite: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    onMuteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan_spin")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_spin"
    )

    val favBgColor by animateColorAsState(
        targetValue = if (isFavorite) Color(0xFFFFEBEE) else GeometricSecondaryContainer.copy(alpha = 0.5f),
        label = "fav_bg_anim"
    )
    val favBorderColor by animateColorAsState(
        targetValue = if (isFavorite) GeometricFavoriteRed.copy(alpha = 0.5f) else GeometricBorderLight,
        label = "fav_border_anim"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(GeometricSurface)
            .border(1.dp, GeometricBorderLight, RoundedCornerShape(26.dp))
            .shadow(4.dp, RoundedCornerShape(26.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Section Header: Station Playback & Live Status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STATION PLAYBACK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GeometricTextSecondary,
                letterSpacing = 1.4.sp
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFF2E7D32) else GeometricTextMuted)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = if (isPlaying) "PLAYING LIVE" else "PAUSED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) Color(0xFF2E7D32) else GeometricTextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Primary Playback Row: PREV | HERO PLAY/PAUSE | NEXT
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PREV Station Button
            FilledTonalButton(
                onClick = onPrevStation,
                enabled = !isScanning,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricSecondaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("station_prev_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous Station",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PREV",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Central Hero PLAY / PAUSE Button
            Box(
                modifier = Modifier
                    .size(66.dp)
                    .shadow(6.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(GeometricPrimary)
                    .clickable { onPlayPauseToggle() }
                    .testTag("play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause FM Radio" else "Play FM Radio",
                    tint = GeometricOnPrimary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // NEXT Station Button
            FilledTonalButton(
                onClick = onNextStation,
                enabled = !isScanning,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricSecondaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(54.dp)
                    .testTag("station_next_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next Station",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Action Deck: AUTO SEARCH (Best Signal) + ADD TO FAVORITE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AUTO SEARCH (Best Signal) Button
            Button(
                onClick = onAutoSearch,
                enabled = !isScanning,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GeometricPrimaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("station_auto_search_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Auto Search Best Signal",
                        tint = GeometricPrimary,
                        modifier = Modifier
                            .size(18.dp)
                            .then(if (isScanning) Modifier.rotate(rotationAngle) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isScanning) "SEARCHING…" else "AUTO SEARCH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = GeometricOnPrimaryContainer,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "Best Signal",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GeometricPrimary
                        )
                    }
                }
            }

            // ADD TO FAVORITE / REMOVE FROM FAVORITE Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(favBgColor)
                    .border(1.dp, favBorderColor, RoundedCornerShape(16.dp))
                    .clickable { onToggleFavorite() }
                    .padding(horizontal = 8.dp)
                    .testTag("station_favorite_button"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                        tint = if (isFavorite) GeometricFavoriteRed else GeometricTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isFavorite) "SAVED" else "FAVORITE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isFavorite) GeometricFavoriteRed else GeometricTextPrimary,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = if (isFavorite) "In Favorites" else "Add to List",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFavorite) GeometricFavoriteRed else GeometricTextSecondary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Volume Control Slider Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GeometricSecondaryContainer.copy(alpha = 0.4f))
                .border(1.dp, GeometricBorderLight, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
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
                    modifier = Modifier.size(18.dp)
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
                    .padding(horizontal = 4.dp)
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
