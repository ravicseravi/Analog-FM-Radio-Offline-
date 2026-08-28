package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RadioStation
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
import java.util.Locale
import kotlin.math.abs

@Composable
fun StationsGuideSection(
    stations: List<RadioStation>,
    currentFrequency: Float,
    favoriteFrequencies: Set<Float>,
    onTuneStation: (Float) -> Unit,
    onSaveCustomName: (Float, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameFreq by remember { mutableStateOf(currentFrequency) }
    var customStationName by remember { mutableStateOf("") }
    var customGenre by remember { mutableStateOf("") }

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
                        .background(GeometricPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = null,
                        tint = GeometricPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "FM BROADCAST GUIDE",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeometricTextPrimary,
                    letterSpacing = 1.2.sp
                )
            }

            // Button to custom-name currently tuned frequency
            FilledTonalButton(
                onClick = {
                    renameFreq = currentFrequency
                    customStationName = stations.find { abs(it.frequency - currentFrequency) < 0.06f }?.name ?: ""
                    customGenre = stations.find { abs(it.frequency - currentFrequency) < 0.06f }?.genre ?: "Custom FM"
                    showRenameDialog = true
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = GeometricPrimaryContainer,
                    contentColor = GeometricOnPrimaryContainer
                ),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("name_station_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Name Current Station",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Name Station", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stations.forEach { station ->
                val isSelected = abs(station.frequency - currentFrequency) < 0.06f
                val isFav = favoriteFrequencies.any { abs(it - station.frequency) < 0.06f }

                val itemBg = if (isSelected) GeometricActivePreset else GeometricSecondaryContainer.copy(alpha = 0.35f)
                val itemBorder = if (isSelected) GeometricPrimary else GeometricBorderLight

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(itemBg)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = itemBorder,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { onTuneStation(station.frequency) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("guide_station_${(station.frequency * 10).toInt()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${station.formattedFrequency} MHz",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) GeometricPrimary else GeometricTextPrimary,
                            modifier = Modifier.width(82.dp)
                        )

                        Column(modifier = Modifier.padding(start = 6.dp)) {
                            Text(
                                text = station.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = GeometricTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = station.callsign,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = GeometricPrimary
                                )
                                Text(text = "•", fontSize = 10.sp, color = GeometricTextMuted)
                                Text(
                                    text = station.genre,
                                    fontSize = 10.sp,
                                    color = GeometricTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isFav) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Favorited",
                                tint = GeometricFavoriteRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        FilledTonalButton(
                            onClick = { onTuneStation(station.frequency) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) GeometricPrimary else GeometricPrimaryContainer,
                                contentColor = if (isSelected) Color.White else GeometricOnPrimaryContainer
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(
                                text = if (isSelected) "TUNED" else "TUNE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Name / Edit Station Dialog
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = {
                Text(
                    text = "Name Radio Station (${String.format(Locale.US, "%.1f", renameFreq)} MHz)",
                    color = GeometricTextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Assign a custom broadcast name to this FM frequency:",
                        color = GeometricTextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = customStationName,
                        onValueChange = { customStationName = it },
                        label = { Text("Station Name (e.g. Jazz Horizon)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeometricPrimary,
                            unfocusedBorderColor = GeometricBorderLight,
                            focusedTextColor = GeometricTextPrimary,
                            unfocusedTextColor = GeometricTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("station_name_input")
                    )
                    OutlinedTextField(
                        value = customGenre,
                        onValueChange = { customGenre = it },
                        label = { Text("Genre (e.g. Jazz, Pop, News)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GeometricPrimary,
                            unfocusedBorderColor = GeometricBorderLight,
                            focusedTextColor = GeometricTextPrimary,
                            unfocusedTextColor = GeometricTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("station_genre_input")
                    )
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        if (customStationName.isNotBlank()) {
                            onSaveCustomName(renameFreq, customStationName, customGenre)
                        }
                        showRenameDialog = false
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = GeometricPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("save_station_name_button")
                ) {
                    Text("Save & Favorite", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = GeometricTextSecondary)
                }
            },
            containerColor = GeometricSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

