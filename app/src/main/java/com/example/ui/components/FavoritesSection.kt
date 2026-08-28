package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import kotlin.math.abs

@Composable
fun FavoritesSection(
    favorites: List<FavoriteStation>,
    currentFrequency: Float,
    isPlaying: Boolean,
    onPlayFavorite: (FavoriteStation) -> Unit,
    onRemoveFavorite: (FavoriteStation) -> Unit,
    onScanRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(GeometricSurface)
            .border(1.dp, GeometricBorderLight, RoundedCornerShape(32.dp))
            .shadow(6.dp, RoundedCornerShape(32.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GeometricFavoriteRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = GeometricFavoriteRed,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "FAVORITE STATIONS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeometricTextPrimary,
                    letterSpacing = 1.2.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(GeometricPrimaryContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${favorites.size} SAVED",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeometricOnPrimaryContainer,
                    letterSpacing = 0.5.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (favorites.isEmpty()) {
            // Empty state with friendly prompt
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(GeometricSecondaryContainer.copy(alpha = 0.35f))
                    .border(1.dp, GeometricBorderLight, RoundedCornerShape(24.dp))
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(GeometricPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = GeometricPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "No favorites saved yet",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeometricTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Scan available frequencies and tap Save to store your favorite radio stations here for instant 1-tap playback.",
                    fontSize = 12.sp,
                    color = GeometricTextSecondary,
                    lineHeight = 17.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = onScanRequested,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GeometricPrimary,
                        contentColor = GeometricOnPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("empty_favorites_scan_button")
                ) {
                    Text(text = "Scan FM Band", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                favorites.forEach { station ->
                    val isCurrent = abs(station.frequency - currentFrequency) < 0.06f
                    FavoriteStationCard(
                        station = station,
                        isCurrentStation = isCurrent,
                        isPlaying = isPlaying && isCurrent,
                        onPlay = { onPlayFavorite(station) },
                        onRemove = { onRemoveFavorite(station) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteStationCard(
    station: FavoriteStation,
    isCurrentStation: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isCurrentStation) GeometricActivePreset else GeometricSecondaryContainer.copy(alpha = 0.35f)
    val cardBorder = if (isCurrentStation) GeometricPrimary else GeometricBorderLight

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg)
            .border(
                width = if (isCurrentStation) 1.5.dp else 1.dp,
                color = cardBorder,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onPlay() }
            .padding(12.dp)
            .testTag("favorite_card_${(station.frequency * 10).toInt()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Frequency badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isCurrentStation) GeometricPrimary else GeometricPrimaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = station.formattedFrequency,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentStation) Color.White else GeometricOnPrimaryContainer
                    )
                    Text(
                        text = "MHz",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isCurrentStation) Color.White.copy(alpha = 0.8f) else GeometricTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Station info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = station.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeometricTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isCurrentStation && isPlaying) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Playing",
                            tint = GeometricPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = station.callsign,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GeometricPrimary
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = GeometricTextMuted
                    )
                    Text(
                        text = station.genre,
                        fontSize = 11.sp,
                        color = GeometricTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons: Play & Delete
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilledTonalButton(
                    onClick = onPlay,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isCurrentStation && isPlaying) GeometricPrimary else GeometricPrimaryContainer,
                        contentColor = if (isCurrentStation && isPlaying) Color.White else GeometricOnPrimaryContainer
                    ),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("play_favorite_${(station.frequency * 10).toInt()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play ${station.name}",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCurrentStation && isPlaying) "ON AIR" else "PLAY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("remove_favorite_${(station.frequency * 10).toInt()}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove ${station.name} from Favorites",
                        tint = GeometricTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

