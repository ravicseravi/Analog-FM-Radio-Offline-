package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@Composable
fun AboutDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(GeometricSurface)
                .border(1.dp, GeometricBorderLight, RoundedCornerShape(28.dp))
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .testTag("about_dialog_surface"),
            color = GeometricSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
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
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "FM RADIO INDIA",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = GeometricTextPrimary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Live Broadcast Player • v1.0",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeometricTextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GeometricSecondaryContainer)
                            .testTag("close_about_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = GeometricTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(GeometricBorderLight)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Developer Information Card (Prominently displays Ravikant Prasad)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GeometricPrimary.copy(alpha = 0.08f))
                        .border(1.5.dp, GeometricPrimary.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(GeometricPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Developer",
                                tint = GeometricOnPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "DEVELOPER",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GeometricPrimary,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "Ravikant Prasad",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = GeometricTextPrimary
                            )
                            Text(
                                text = "Lead Android & Radio Systems Developer",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = GeometricTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Contact & App Details List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(GeometricSecondaryContainer.copy(alpha = 0.45f))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    
                    AboutDetailItem(
                        icon = Icons.Default.Star,
                        label = "Technology Stack",
                        value = "Kotlin • Jetpack Compose • Media3 ExoPlayer"
                    )
                    AboutDetailItem(
                        icon = Icons.Default.Headphones,
                        label = "Audio Streaming",
                        value = "Official 128kbps AAC/MP3 Live Indian FM Feeds"
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Help Section
                Text(
                    text = "HELP & USER GUIDE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeometricTextSecondary,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                HelpTipRow(
                    tipNumber = "1",
                    tipText = "Tap AUTO SEARCH to sweep the FM band and lock onto the strongest live station automatically."
                )

                HelpTipRow(
                    tipNumber = "2",
                    tipText = "Use PREV & NEXT in the player deck to jump directly between live Indian FM stations."
                )

                HelpTipRow(
                    tipNumber = "3",
                    tipText = "Tap the Heart button to bookmark stations into your Favorites tab for 1-tap instant playback."
                )

                HelpTipRow(
                    tipNumber = "4",
                    tipText = "Background playback continues seamlessly even when the app is minimized or screen is locked."
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Dismiss Button
                Button(
                    onClick = onDismissRequest,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeometricPrimary,
                        contentColor = GeometricOnPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Got It",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GeometricSurface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = GeometricPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GeometricTextSecondary
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GeometricTextPrimary
            )
        }
    }
}

@Composable
private fun HelpTipRow(
    tipNumber: String,
    tipText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(GeometricPrimaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = tipNumber,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = GeometricPrimary
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = tipText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = GeometricTextSecondary,
            lineHeight = 17.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
