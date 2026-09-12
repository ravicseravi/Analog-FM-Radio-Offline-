package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AboutDialog
import com.example.ui.components.FavoritesSection
import com.example.ui.components.StationDialRibbon
import com.example.ui.components.StationsGuideSection
import com.example.ui.components.TunerControls
import com.example.ui.components.TunerDisplay
import com.example.ui.theme.GeometricActivePreset
import com.example.ui.theme.GeometricBackground
import com.example.ui.theme.GeometricBorderLight
import com.example.ui.theme.GeometricFavoriteRed
import com.example.ui.theme.GeometricOnPrimaryContainer
import com.example.ui.theme.GeometricPrimary
import com.example.ui.theme.GeometricPrimaryContainer
import com.example.ui.theme.GeometricSecondaryContainer
import com.example.ui.theme.GeometricSurface
import com.example.ui.theme.GeometricTextMuted
import com.example.ui.theme.GeometricTextPrimary
import com.example.ui.theme.GeometricTextSecondary
import kotlinx.coroutines.delay

@Composable
fun RadioScreen(
    viewModel: RadioViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val equalizerBars by viewModel.equalizerBars.collectAsState()

    // Auto-dismiss status message after 3 seconds
    LaunchedEffect(uiState.scanStatusMessage) {
        if (uiState.scanStatusMessage != null) {
            delay(3000)
            viewModel.clearStatusMessage()
        }
    }

    // Safe, crash-proof About & Developer info dialog
    if (uiState.showAboutDialog) {
        AboutDialog(
            onDismissRequest = { viewModel.closeAboutDialog() }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(GeometricBackground),
        containerColor = GeometricBackground,
        topBar = {
            RadioHeader(
                isScanning = uiState.isScanning,
                isPlaying = uiState.isPlaying,
                onOpenAbout = { viewModel.openAboutDialog() }
            )
        },
        bottomBar = {
            RadioBottomNavigation(
                selectedTab = uiState.selectedTab,
                favoritesCount = uiState.favorites.size,
                onTabSelected = { viewModel.setSelectedTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Content
            when (uiState.selectedTab) {
                RadioTab.TUNER -> {
                    // Responsive, autofit layout that gracefully occupies the full mobile display height
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val isCompact = maxHeight < 620.dp
                        val scrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(if (isCompact) Modifier.verticalScroll(scrollState) else Modifier)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = if (isCompact) Arrangement.spacedBy(10.dp) else Arrangement.SpaceBetween
                        ) {
                            // 1. Digital Tuner Display Card
                            TunerDisplay(
                                frequency = uiState.currentFrequency,
                                station = uiState.currentStation,
                                isPlaying = uiState.isPlaying,
                                isScanning = uiState.isScanning,
                                signalStrength = uiState.signalStrength,
                                rdsText = uiState.rdsText,
                                equalizerBars = equalizerBars
                            )

                            // 2. Interactive FM Station Dial Strip (Horizontal spectrum)
                            StationDialRibbon(
                                stations = uiState.allStations,
                                currentFrequency = uiState.currentFrequency,
                                isPlaying = uiState.isPlaying,
                                onStationSelect = { station -> viewModel.tuneToStation(station) }
                            )

                            // 3. Station Controls & Playback Deck
                            TunerControls(
                                isPlaying = uiState.isPlaying,
                                isScanning = uiState.isScanning,
                                isFavorite = uiState.isCurrentFavorite,
                                currentStation = uiState.currentStation,
                                currentFrequency = uiState.currentFrequency,
                                volume = uiState.volume,
                                isMuted = uiState.isMuted,
                                onPlayPauseToggle = { viewModel.togglePlayPause() },
                                onPrevStation = { viewModel.scanNext(forward = false) },
                                onNextStation = { viewModel.scanNext(forward = true) },
                                onAutoSearch = { viewModel.autoSearchBestStation() },
                                onToggleFavorite = { viewModel.toggleFavorite() },
                                onVolumeChange = { vol -> viewModel.setVolume(vol) },
                                onMuteToggle = { viewModel.toggleMute() }
                            )
                        }
                    }
                }

                RadioTab.FAVORITES -> {
                    // Full list of saved favorites with 1-tap playback and management
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        FavoritesSection(
                            favorites = uiState.favorites,
                            currentFrequency = uiState.currentFrequency,
                            isPlaying = uiState.isPlaying,
                            onPlayFavorite = { viewModel.playFromFavorite(it) },
                            onRemoveFavorite = { viewModel.removeFavorite(it) },
                            onScanRequested = {
                                viewModel.setSelectedTab(RadioTab.TUNER)
                                viewModel.autoSearchBestStation()
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                RadioTab.GUIDE -> {
                    // Full FM band frequency guide with stations & custom naming
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        StationsGuideSection(
                            stations = uiState.allStations,
                            currentFrequency = uiState.currentFrequency,
                            favoriteFrequencies = uiState.favorites.map { it.frequency }.toSet(),
                            onTuneStation = { freq ->
                                viewModel.tuneTo(freq)
                            },
                            onSaveCustomName = { freq, name, genre ->
                                viewModel.saveCustomStationName(freq, name, genre)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            // Notification pill banner when scanning locks or station changes
            AnimatedVisibility(
                visible = uiState.scanStatusMessage != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            ) {
                uiState.scanStatusMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(GeometricSurface)
                            .border(1.dp, GeometricBorderLight, RoundedCornerShape(24.dp))
                            .padding(horizontal = 18.dp, vertical = 10.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(GeometricPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                color = GeometricTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RadioHeader(
    isScanning: Boolean,
    isPlaying: Boolean,
    onOpenAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = GeometricBackground,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(GeometricPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radio,
                        contentDescription = "FM Radio App",
                        tint = GeometricPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FM RADIO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = GeometricTextPrimary,
                        letterSpacing = 1.0.sp
                    )
                    // Live FM status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE FM BROADCAST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            // Top-Right: Help & Developer About Button
            IconButton(
                onClick = onOpenAbout,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(GeometricSecondaryContainer)
                    .border(1.dp, GeometricBorderLight, CircleShape)
                    .testTag("header_about_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "About & Help",
                    tint = GeometricPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RadioBottomNavigation(
    selectedTab: RadioTab,
    favoritesCount: Int,
    onTabSelected: (RadioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = GeometricSurface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = selectedTab == RadioTab.TUNER,
            onClick = { onTabSelected(RadioTab.TUNER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "FM Tuner",
                    tint = if (selectedTab == RadioTab.TUNER) GeometricPrimary else GeometricTextSecondary
                )
            },
            label = {
                Text(
                    text = "Tuner",
                    fontWeight = if (selectedTab == RadioTab.TUNER) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == RadioTab.TUNER) GeometricPrimary else GeometricTextSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = GeometricPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_tuner")
        )

        NavigationBarItem(
            selected = selectedTab == RadioTab.FAVORITES,
            onClick = { onTabSelected(RadioTab.FAVORITES) },
            icon = {
                Box {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Saved Favorites",
                        tint = if (selectedTab == RadioTab.FAVORITES) GeometricFavoriteRed else GeometricTextSecondary
                    )
                    if (favoritesCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(GeometricFavoriteRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$favoritesCount",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            label = {
                Text(
                    text = "Favorites",
                    fontWeight = if (selectedTab == RadioTab.FAVORITES) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == RadioTab.FAVORITES) GeometricPrimary else GeometricTextSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = GeometricPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_favorites")
        )

        NavigationBarItem(
            selected = selectedTab == RadioTab.GUIDE,
            onClick = { onTabSelected(RadioTab.GUIDE) },
            icon = {
                Icon(
                    imageVector = Icons.Default.FormatListBulleted,
                    contentDescription = "Stations Guide",
                    tint = if (selectedTab == RadioTab.GUIDE) GeometricPrimary else GeometricTextSecondary
                )
            },
            label = {
                Text(
                    text = "Stations",
                    fontWeight = if (selectedTab == RadioTab.GUIDE) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == RadioTab.GUIDE) GeometricPrimary else GeometricTextSecondary
                )
            },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = GeometricPrimaryContainer
            ),
            modifier = Modifier.testTag("nav_guide")
        )
    }
}
