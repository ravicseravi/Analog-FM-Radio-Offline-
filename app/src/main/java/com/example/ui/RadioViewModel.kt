package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.RadioAudioPlayer
import com.example.data.RadioRepository
import com.example.data.db.FavoriteStation
import com.example.model.RadioStation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

import java.util.Locale
import kotlinx.coroutines.isActive

enum class RadioTab {
    TUNER,
    FAVORITES,
    GUIDE
}

data class RadioUiState(
    val currentFrequency: Float = 98.3f, // Default Indian standard: Radio Mirchi 98.3 MHz
    val currentStation: RadioStation? = null,
    val isPlaying: Boolean = true,
    val isScanning: Boolean = false,
    val isMuted: Boolean = false,
    val volume: Float = 0.85f,
    val signalStrength: Int = 100,
    val rdsText: String = "Radio Mirchi 98.3 • Now Playing: Kesariya - Arijit Singh",
    val isCurrentFavorite: Boolean = true,
    val favorites: List<FavoriteStation> = emptyList(),
    val allStations: List<RadioStation> = emptyList(),
    val selectedTab: RadioTab = RadioTab.TUNER,
    val scanStatusMessage: String? = null,
    val stereoActive: Boolean = true,
    val isLiveStream: Boolean = false,
    val isBuffering: Boolean = false
)

class RadioViewModel(
    private val repository: RadioRepository,
    private val audioPlayer: RadioAudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(RadioUiState())
    val uiState: StateFlow<RadioUiState> = _uiState.asStateFlow()

    val equalizerBars: StateFlow<List<Float>> = audioPlayer.equalizerBars

    private var scanJob: Job? = null
    private var songTickerJob: Job? = null

    init {
        // Pre-seed defaults if database is empty on first run
        viewModelScope.launch {
            repository.seedDefaultsIfEmpty()
        }

        // Observe favorites from Room DB reactively
        viewModelScope.launch {
            repository.getFavorites().collect { favs ->
                _uiState.update { state ->
                    val isFav = favs.any { abs(it.frequency - state.currentFrequency) < 0.06f }
                    state.copy(
                        favorites = favs,
                        isCurrentFavorite = isFav
                    )
                }
            }
        }

        // Observe live stream status and buffering
        viewModelScope.launch {
            audioPlayer.isLiveStream.collect { live ->
                _uiState.update { it.copy(isLiveStream = live) }
            }
        }
        viewModelScope.launch {
            audioPlayer.isBuffering.collect { buffering ->
                _uiState.update { it.copy(isBuffering = buffering) }
            }
        }

        // Initialize with default Indian station: Radio Mirchi (98.3 MHz)
        val all = repository.getAllStations()
        val initialStation = repository.findStationAt(98.3f) ?: all.firstOrNull()
        val initialFreq = initialStation?.frequency ?: 98.3f

        _uiState.update {
            it.copy(
                currentFrequency = initialFreq,
                currentStation = initialStation,
                allStations = all,
                signalStrength = initialStation?.signalStrength ?: 100,
                rdsText = initialStation?.rdsSample ?: "Radio Mirchi 98.3 • Now Playing: Kesariya - Arijit Singh"
            )
        }

        // Start playback on initial load
        if (initialStation != null) {
            audioPlayer.playStation(
                frequency = initialStation.frequency,
                streamUrl = initialStation.streamUrl,
                fallbackUrl = initialStation.fallbackUrl
            )
        }

        startSongTicker()
    }

    /**
     * Tunes into a specific frequency.
     */
    fun tuneTo(frequency: Float, autoPlay: Boolean = true) {
        val rounded = (frequency * 10f).roundToInt() / 10f
        val clamped = rounded.coerceIn(87.5f, 108.0f)
        val station = repository.findStationAt(clamped)

        val isFav = _uiState.value.favorites.any { abs(it.frequency - clamped) < 0.06f }
        val signal = if (station != null) {
            station.signalStrength
        } else {
            // Off-frequency background RF level
            (10 + (clamped * 17).toInt() % 25)
        }

        val rds = station?.rdsSample ?: "FM ${String.format(Locale.US, "%.1f", clamped)} MHz • Scanning Indian Band…"

        _uiState.update {
            it.copy(
                currentFrequency = clamped,
                currentStation = station,
                isCurrentFavorite = isFav,
                signalStrength = signal,
                rdsText = rds,
                scanStatusMessage = if (station != null) "Tuned to ${station.name}" else null,
                stereoActive = station != null
            )
        }

        if (autoPlay && _uiState.value.isPlaying) {
            audioPlayer.playStation(
                frequency = clamped,
                streamUrl = station?.streamUrl ?: "",
                fallbackUrl = station?.fallbackUrl ?: ""
            )
        }
    }

    /**
     * Scans for the next available radio frequency as per Indian 100 kHz raster.
     */
    fun scanNext(forward: Boolean = true) {
        scanJob?.cancel()
        scanJob = viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanStatusMessage = if (forward) "Scanning Indian FM band up…" else "Scanning Indian FM band down…") }
            audioPlayer.playTuningBurst()

            val currentFreq = _uiState.value.currentFrequency
            val targetStation = repository.scanNextStation(currentFreq, forward)

            // Step through intermediate frequencies with Indian standard 100 kHz (0.1 MHz) channel spacing
            val stepDirection = if (forward) 0.1f else -0.1f
            var sweepFreq = currentFreq
            repeat(6) {
                sweepFreq += stepDirection
                if (sweepFreq > 108.0f) sweepFreq = 87.5f
                if (sweepFreq < 87.5f) sweepFreq = 108.0f
                _uiState.update {
                    it.copy(
                        currentFrequency = (sweepFreq * 10f).roundToInt() / 10f,
                        signalStrength = (25..50).random()
                    )
                }
                audioPlayer.playTuningBurst()
                delay(70)
            }

            // Lock onto target Indian station
            tuneTo(targetStation.frequency, autoPlay = true)
            _uiState.update {
                it.copy(
                    isScanning = false,
                    isPlaying = true,
                    scanStatusMessage = "Locked: ${targetStation.name} (${targetStation.formattedFrequency} MHz)"
                )
            }
        }
    }

    /**
     * Steps frequency by 0.1 MHz (+/- 0.1) as per Indian 100 kHz standard.
     */
    fun stepFineTune(delta: Float) {
        val newFreq = ((_uiState.value.currentFrequency + delta) * 10f).roundToInt() / 10f
        audioPlayer.playTuningBurst()
        tuneTo(newFreq)
    }

    /**
     * Toggles playback pause / resume.
     */
    fun togglePlayPause() {
        val currentPlaying = _uiState.value.isPlaying
        if (currentPlaying) {
            audioPlayer.pause()
            _uiState.update { it.copy(isPlaying = false) }
        } else {
            val freq = _uiState.value.currentFrequency
            val station = _uiState.value.currentStation
            val url = station?.streamUrl ?: ""
            val fallback = station?.fallbackUrl ?: ""
            audioPlayer.resume(freq, url, fallback)
            _uiState.update { it.copy(isPlaying = true) }
        }
    }

    /**
     * Toggles favorite status for currently tuned station.
     */
    fun toggleFavorite() {
        val state = _uiState.value
        val freq = state.currentFrequency
        val isFav = state.isCurrentFavorite

        viewModelScope.launch {
            if (isFav) {
                repository.deleteFavoriteByFrequency(freq)
                _uiState.update {
                    it.copy(
                        isCurrentFavorite = false,
                        scanStatusMessage = "Removed from Favorites"
                    )
                }
            } else {
                val station = state.currentStation
                val newFavorite = FavoriteStation(
                    frequency = freq,
                    name = station?.name ?: "FM Station ${String.format("%.1f", freq)}",
                    callsign = station?.callsign ?: "FM-${(freq * 10).toInt()}",
                    genre = station?.genre ?: "Custom FM",
                    streamUrl = station?.streamUrl ?: ""
                )
                repository.saveFavorite(newFavorite)
                _uiState.update {
                    it.copy(
                        isCurrentFavorite = true,
                        scanStatusMessage = "Added to Favorites"
                    )
                }
            }
        }
    }

    /**
     * Plays immediately from a FavoriteStation.
     */
    fun playFromFavorite(favorite: FavoriteStation) {
        tuneTo(favorite.frequency, autoPlay = true)
        _uiState.update {
            it.copy(
                isPlaying = true,
                selectedTab = RadioTab.TUNER,
                scanStatusMessage = "Playing Favorite: ${favorite.name}"
            )
        }
    }

    /**
     * Removes a station from favorites list.
     */
    fun removeFavorite(favorite: FavoriteStation) {
        viewModelScope.launch {
            repository.deleteFavorite(favorite)
            if (abs(_uiState.value.currentFrequency - favorite.frequency) < 0.06f) {
                _uiState.update { it.copy(isCurrentFavorite = false) }
            }
        }
    }

    /**
     * Saves or edits station name for the current frequency.
     */
    fun saveCustomStationName(frequency: Float, customName: String, genre: String = "Custom") {
        if (customName.isBlank()) return
        viewModelScope.launch {
            val station = FavoriteStation(
                frequency = frequency,
                name = customName.trim(),
                callsign = "FM-${(frequency * 10).toInt()}",
                genre = genre.trim()
            )
            repository.saveFavorite(station)
            tuneTo(frequency)
        }
    }

    fun setVolume(vol: Float) {
        _uiState.update { it.copy(volume = vol, isMuted = false) }
        audioPlayer.setVolume(vol)
    }

    fun toggleMute() {
        val muted = audioPlayer.toggleMute()
        _uiState.update { it.copy(isMuted = muted) }
    }

    fun setSelectedTab(tab: RadioTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(scanStatusMessage = null) }
    }

    private fun startSongTicker() {
        songTickerJob?.cancel()
        songTickerJob = viewModelScope.launch {
            val songCatalog = mapOf(
                98.3f to listOf(
                    "Radio Mirchi 98.3 • Now Playing: Kesariya - Arijit Singh",
                    "Radio Mirchi 98.3 • Now Playing: Deewani Mastani - Shreya Ghoshal",
                    "Radio Mirchi 98.3 • Now Playing: Chaleya - Anirudh & Arijit Singh",
                    "Radio Mirchi 98.3 • On Air: Mirchi Top 20 Countdown"
                ),
                93.5f to listOf(
                    "Red FM 93.5 • Now Playing: Jhoome Jo Pathaan - Vishal-Shekhar",
                    "Red FM 93.5 • Now Playing: Apna Bana Le - Sachin-Jigar & Arijit",
                    "Red FM 93.5 • Now Playing: Raataan Lambiyan - Jubin Nautiyal",
                    "Red FM 93.5 • On Air: Bajaate Raho with RJ Raunac"
                ),
                92.7f to listOf(
                    "BIG FM 92.7 • Now Playing: Kuch Kuch Hota Hai - Udit Narayan & Alka Yagnik",
                    "BIG FM 92.7 • Now Playing: Tujhe Dekha Toh Yeh Jaana Sanam - Kumar Sanu",
                    "BIG FM 92.7 • Now Playing: Pehla Nasha - Udit Narayan",
                    "BIG FM 92.7 • On Air: Suhaana Safar with Annu Kapoor"
                ),
                102.6f to listOf(
                    "AIR FM Gold 102.6 • Now Playing: Pal Pal Dil Ke Paas - Kishore Kumar",
                    "AIR FM Gold 102.6 • Now Playing: Yeh Shaam Mastani - Kishore Kumar",
                    "AIR FM Gold 102.6 • Now Playing: O Saathi Re - Kishore Kumar",
                    "AIR FM Gold 102.6 • On Air: Akashvani Golden Classics"
                ),
                107.1f to listOf(
                    "AIR Vividh Bharati • Now Playing: Lag Ja Gale - Lata Mangeshkar",
                    "AIR Vividh Bharati • Now Playing: Tere Bina Zindagi Se - Kishore & Lata",
                    "AIR Vividh Bharati • Now Playing: Kabhi Kabhie Mere Dil Mein - Mukesh",
                    "AIR Vividh Bharati • On Air: Sangeet Sarita & Chhaya Geet"
                ),
                91.1f to listOf(
                    "Radio City 91.1 • Now Playing: Kesariya - Arijit Singh",
                    "Radio City 91.1 • Now Playing: Kal Ho Naa Ho - Sonu Nigam",
                    "Radio City 91.1 • Now Playing: Chaiyya Chaiyya - Sukhwinder Singh",
                    "Radio City 91.1 • Rag Rag Mein Daude City"
                ),
                95.0f to listOf(
                    "Mirchi Love 95.0 • Now Playing: Jeena Jeena - Atif Aslam",
                    "Mirchi Love 95.0 • Now Playing: Tera Ban Jaunga - Akhil Sachdeva",
                    "Mirchi Love 95.0 • Now Playing: Agar Tum Saath Ho - Alka Yagnik & Arijit"
                ),
                104.0f to listOf(
                    "Fever 104 FM • Now Playing: Aankhon Mein Teri - KK",
                    "Fever 104 FM • Now Playing: Desi Girl - Shankar-Ehsaan-Loy",
                    "Fever 104 FM • Baap of Bollywood Hits"
                ),
                104.8f to listOf(
                    "Ishq 104.8 FM • Now Playing: Likhe Jo Khat Tujhe - Mohd. Rafi",
                    "Ishq 104.8 FM • Now Playing: Chaudhvin Ka Chand - Mohd. Rafi",
                    "Ishq 104.8 FM • Do The Ishq Baby"
                ),
                106.4f to listOf(
                    "Magic FM 106.4 • Now Playing: Kar Gayi Chull - Badshah & Neha Kakkar",
                    "Magic FM 106.4 • Now Playing: Gallan Goodiyaan - Shankar Mahadevan",
                    "Magic FM 106.4 • Jee Le Zindagi"
                ),
                94.3f to listOf(
                    "MY FM 94.3 • Now Playing: Lut Gaye - Jubin Nautiyal",
                    "MY FM 94.3 • Now Playing: Dil Diyan Gallan - Atif Aslam",
                    "MY FM 94.3 • Jiyo Dil Se!"
                ),
                100.7f to listOf(
                    "AIR FM Rainbow 100.7 • Now Playing: Aap Ki Nazron Ne Samjha - Lata Mangeshkar",
                    "AIR FM Rainbow 100.7 • Now Playing: Mere Samne Wali Khidki - Kishore Kumar",
                    "AIR FM Rainbow 100.7 • Akashvani National Broadcast"
                )
            )

            var songIndex = 0
            while (isActive) {
                delay(7000)
                val currentFreq = _uiState.value.currentFrequency
                val matchingFreq = songCatalog.keys.find { abs(it - currentFreq) < 0.1f }
                if (matchingFreq != null && _uiState.value.isPlaying) {
                    val songs = songCatalog[matchingFreq] ?: emptyList()
                    if (songs.isNotEmpty()) {
                        val nextSong = songs[(songIndex++) % songs.size]
                        _uiState.update { it.copy(rdsText = nextSong) }
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        songTickerJob?.cancel()
        audioPlayer.release()
    }
}

class RadioViewModelFactory(
    private val repository: RadioRepository,
    private val audioPlayer: RadioAudioPlayer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RadioViewModel::class.java)) {
            return RadioViewModel(repository, audioPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
