package com.example.audio

import android.content.Context
import android.media.AudioAttributes as AndroidAudioAttributes
import android.media.SoundPool
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import android.os.Handler
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.example.R
import com.example.model.EqualizerBand
import com.example.model.EqualizerSettings
import com.example.model.SoundPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

private const val TAG = "RadioAudioPlayer"

/**
 * Audio-only RenderersFactory that prevents querying video decoders and hardware video interfaces.
 * This resolves "Failed to query component interface for required system resources: 6".
 */
private class AudioOnlyRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    init {
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_OFF)
        setEnableDecoderFallback(true)
    }

    override fun buildVideoRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        eventHandler: Handler,
        eventListener: VideoRendererEventListener,
        allowedVideoJoiningTimeMs: Long,
        out: ArrayList<Renderer>
    ) {
        // No video renderers constructed - purely audio playback
    }

    override fun buildImageRenderers(out: ArrayList<Renderer>) {
        // No image renderers constructed
    }
}

/**
 * Bulletproof FM Audio Player engine powered by AndroidX Media3 ExoPlayer.
 *
 * Architecture:
 * 1. Single consolidated ExoPlayer instance for all primary station audio (live streams & offline broadcasts).
 * 2. Audio-only RenderersFactory to prevent resource contention and codec initialization errors.
 * 3. Dedicated low-latency SoundPool for non-blocking tuning bursts and background static RF hiss.
 * 4. Real-time 8-band audio spectrum visualizer synchronized with playback state.
 */
class RadioAudioPlayer(private val context: Context) {
    private val mainScope = CoroutineScope(Dispatchers.Main)

    // Single unified primary player for station broadcast & live streaming
    private var radioPlayer: ExoPlayer? = null

    // Low-latency SoundPool for static hiss and tuning bursts
    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AndroidAudioAttributes.Builder()
                .setUsage(AndroidAudioAttributes.USAGE_MEDIA)
                .setContentType(AndroidAudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var staticBurstSoundId: Int = 0
    private var isSoundPoolLoaded = false
    private var hissStreamId: Int = 0
    private var isHissActive = false

    private var isStationPlaying = false
    private var hasTriedFallback = false

    // Hardware/System Audio Effects
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentAudioSessionId: Int = C.AUDIO_SESSION_ID_UNSET

    private val _equalizerSettings = MutableStateFlow(EqualizerSettings())
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    private var visualizerJob: Job? = null
    private val _equalizerBars = MutableStateFlow(List(8) { 0.05f })
    val equalizerBars: StateFlow<List<Float>> = _equalizerBars.asStateFlow()

    private var currentVolume: Float = 0.90f
    private var isMuted: Boolean = false

    private var currentFrequency: Float = 98.3f
    private var currentStreamUrl: String = ""
    private var currentFallbackUrl: String = ""

    private val _isLiveStream = MutableStateFlow(false)
    val isLiveStream: StateFlow<Boolean> = _isLiveStream.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private var isOfflineMode: Boolean = false

    init {
        initSoundPool()
        initRadioPlayer()
        startVisualizerLoop()
    }

    private fun initSoundPool() {
        try {
            staticBurstSoundId = soundPool.load(context, R.raw.tuning_static, 1)
            soundPool.setOnLoadCompleteListener { _, sampleId, status ->
                if (status == 0 && sampleId == staticBurstSoundId) {
                    isSoundPoolLoaded = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load static burst in SoundPool", e)
        }
    }

    private fun initRadioPlayer() {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()

            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("FMRadioIndia/1.0 (Android; Linux) Media3/1.2.0")
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(8000)
                .setReadTimeoutMs(15000)

            val mediaSourceFactory = DefaultMediaSourceFactory(context)
                .setDataSourceFactory(httpDataSourceFactory)

            val player = ExoPlayer.Builder(context, AudioOnlyRenderersFactory(context))
                .setMediaSourceFactory(mediaSourceFactory)
                .setAudioAttributes(audioAttributes, true)
                .setWakeMode(C.WAKE_MODE_NETWORK)
                .setHandleAudioBecomingNoisy(true)
                .build()

            player.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            _isBuffering.value = false
                            if (!isOfflineMode && currentStreamUrl.isNotBlank()) {
                                _isLiveStream.value = true
                            }
                            val sessionId = player.audioSessionId
                            if (sessionId != C.AUDIO_SESSION_ID_UNSET && sessionId > 0) {
                                attachAudioEffects(sessionId)
                            }
                        }
                        Player.STATE_BUFFERING -> {
                            _isBuffering.value = true
                        }
                        Player.STATE_ENDED -> {
                            player.seekTo(0)
                            player.play()
                        }
                        Player.STATE_IDLE -> Unit
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        _isBuffering.value = false
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.w(TAG, "Playback error: ${error.message}. Fallback available: ${currentFallbackUrl.isNotBlank()}")

                    if (!hasTriedFallback && currentFallbackUrl.isNotBlank()) {
                        hasTriedFallback = true
                        try {
                            player.setMediaItem(MediaItem.fromUri(currentFallbackUrl))
                            player.prepare()
                            player.play()
                            return
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed fallback stream: ${e.message}")
                        }
                    }

                    _isLiveStream.value = false
                    _isBuffering.value = false

                    // Fallback to offline station audio so user never experiences silence
                    if (isStationPlaying && isNearAnyStation(currentFrequency)) {
                        playLocalBroadcast(getStationRawRes(currentFrequency))
                    }
                }
            })

            val vol = if (isMuted) 0f else currentVolume
            player.volume = vol
            radioPlayer = player
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize unified radio player", e)
        }
    }

    fun setOfflineMode(offline: Boolean) {
        isOfflineMode = offline
        if (offline) {
            _isLiveStream.value = false
            _isBuffering.value = false
            if (isStationPlaying && isNearAnyStation(currentFrequency)) {
                val rawResId = getStationRawRes(currentFrequency)
                playLocalBroadcast(rawResId)
            }
        } else if (isStationPlaying && currentStreamUrl.isNotBlank()) {
            startLiveStream(currentStreamUrl, currentFallbackUrl)
        }
    }

    fun isOffline(): Boolean = isOfflineMode

    fun updateServiceNotification(
        stationName: String,
        frequency: Float,
        rdsText: String,
        isPlaying: Boolean
    ) {
        RadioPlaybackService.updateNotification(
            context = context,
            stationName = stationName,
            frequency = frequency,
            rdsText = rdsText,
            isPlaying = isPlaying,
            isOffline = isOfflineMode
        )
    }

    /**
     * Tunes into a radio frequency and starts audio playback.
     */
    fun playStation(frequency: Float, streamUrl: String, fallbackUrl: String = "") {
        currentFrequency = frequency
        currentStreamUrl = streamUrl
        currentFallbackUrl = fallbackUrl
        isStationPlaying = true
        hasTriedFallback = false

        mainScope.launch {
            playTuningBurst()

            val hasStation = isNearAnyStation(frequency)

            if (!hasStation) {
                // Tuned to empty frequency: play soft analog static
                stopRadioPlayer()
                playStaticHiss()
                _isLiveStream.value = false
                _isBuffering.value = false
                return@launch
            }

            // Real Indian FM station tuned!
            stopStaticHiss()

            if (!isOfflineMode && streamUrl.isNotBlank()) {
                startLiveStream(streamUrl, fallbackUrl)
            } else {
                _isLiveStream.value = false
                _isBuffering.value = false
                val rawResId = getStationRawRes(frequency)
                playLocalBroadcast(rawResId)
            }
        }
    }

    private fun playLocalBroadcast(rawResId: Int) {
        try {
            val player = radioPlayer ?: return
            val uri = RawResourceDataSource.buildRawResourceUri(rawResId)
            player.stop()
            player.repeatMode = Player.REPEAT_MODE_ALL
            player.setMediaItem(MediaItem.fromUri(uri))
            val vol = if (isMuted) 0f else currentVolume
            player.volume = vol
            player.prepare()
            player.play()
            _isLiveStream.value = false
            _isBuffering.value = false
            Log.d(TAG, "Playing local broadcast (resId=$rawResId)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play local broadcast", e)
        }
    }

    private fun startLiveStream(url: String, fallbackUrl: String) {
        _isBuffering.value = true
        _isLiveStream.value = false

        try {
            val player = radioPlayer ?: return
            player.stop()
            player.repeatMode = Player.REPEAT_MODE_OFF
            val vol = if (isMuted) 0f else currentVolume
            player.volume = vol
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start live stream", e)
            _isLiveStream.value = false
            _isBuffering.value = false
            val rawResId = getStationRawRes(currentFrequency)
            playLocalBroadcast(rawResId)
        }
    }

    fun playTuningBurst() {
        try {
            if (isSoundPoolLoaded && staticBurstSoundId != 0) {
                val vol = if (isMuted) 0f else (currentVolume * 0.35f)
                soundPool.play(staticBurstSoundId, vol, vol, 1, 0, 1.0f)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tuning burst via SoundPool", e)
        }
    }

    private fun playStaticHiss() {
        try {
            if (isSoundPoolLoaded && staticBurstSoundId != 0 && !isHissActive) {
                val vol = if (isMuted) 0f else (currentVolume * 0.15f)
                hissStreamId = soundPool.play(staticBurstSoundId, vol, vol, 0, -1, 0.9f)
                isHissActive = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing static hiss via SoundPool", e)
        }
    }

    private fun stopStaticHiss() {
        try {
            if (isHissActive && hissStreamId != 0) {
                soundPool.stop(hissStreamId)
                hissStreamId = 0
                isHissActive = false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping static hiss", e)
        }
    }

    private fun stopRadioPlayer() {
        radioPlayer?.stop()
    }

    /**
     * Pauses all audio playback.
     */
    fun pause() {
        isStationPlaying = false
        mainScope.launch {
            radioPlayer?.pause()
            stopStaticHiss()
            _isBuffering.value = false
        }
    }

    /**
     * Resumes audio playback.
     */
    fun resume(frequency: Float, streamUrl: String, fallbackUrl: String) {
        isStationPlaying = true
        mainScope.launch {
            if (isNearAnyStation(frequency)) {
                stopStaticHiss()
                if (radioPlayer != null && radioPlayer?.playbackState == Player.STATE_READY) {
                    radioPlayer?.play()
                } else if (!isOfflineMode && streamUrl.isNotBlank()) {
                    startLiveStream(streamUrl, fallbackUrl)
                } else {
                    val rawResId = getStationRawRes(frequency)
                    playLocalBroadcast(rawResId)
                }
            } else {
                playStaticHiss()
            }
        }
    }

    /**
     * Stops all active playback.
     */
    fun stopPlayback() {
        mainScope.launch {
            stopRadioPlayer()
            stopStaticHiss()
            _isLiveStream.value = false
            _isBuffering.value = false
        }
    }

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
        mainScope.launch {
            val effVol = if (isMuted) 0f else currentVolume
            radioPlayer?.volume = effVol
            if (isHissActive && hissStreamId != 0) {
                soundPool.setVolume(hissStreamId, effVol * 0.15f, effVol * 0.15f)
            }
        }
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        mainScope.launch {
            val effVol = if (isMuted) 0f else currentVolume
            radioPlayer?.volume = effVol
            if (isHissActive && hissStreamId != 0) {
                soundPool.setVolume(hissStreamId, effVol * 0.15f, effVol * 0.15f)
            }
        }
    }

    fun toggleMute(): Boolean {
        val newMute = !isMuted
        setMuted(newMute)
        return newMute
    }

    private fun isNearAnyStation(frequency: Float): Boolean {
        val stationFreqs = floatArrayOf(
            91.1f, 92.7f, 93.5f, 94.3f, 95.0f, 98.3f,
            100.7f, 102.6f, 104.0f, 104.8f, 106.4f, 107.1f
        )
        return stationFreqs.any { abs(it - frequency) < 0.18f }
    }

    private fun getStationRawRes(frequency: Float): Int {
        return when {
            abs(frequency - 98.3f) < 0.18f -> R.raw.station_mirchi   // Radio Mirchi 98.3 (Bollywood Hits)
            abs(frequency - 102.6f) < 0.18f -> R.raw.station_gold    // AIR FM Gold 102.6 (Golden Era Classics)
            abs(frequency - 107.1f) < 0.18f -> R.raw.station_vividh  // AIR Vividh Bharati 107.1 (Akashvani Theme & Classics)
            abs(frequency - 93.5f) < 0.18f -> R.raw.station_redfm    // Red FM 93.5 ("Bajaate Raho" Upbeat)
            abs(frequency - 92.7f) < 0.18f -> R.raw.station_bigfm    // BIG FM 92.7 (90s Romance Melodies)
            abs(frequency - 91.1f) < 0.18f -> R.raw.station_city     // Radio City 91.1 (Pop & Indie)
            abs(frequency - 100.7f) < 0.18f -> R.raw.station_rainbow // AIR FM Rainbow 100.7 (Classics & Pop)
            abs(frequency - 95.0f) < 0.18f -> R.raw.station_bigfm    // Mirchi Love 95.0
            abs(frequency - 104.8f) < 0.18f -> R.raw.station_gold    // Ishq 104.8
            abs(frequency - 104.0f) < 0.18f -> R.raw.station_redfm   // Fever 104
            abs(frequency - 106.4f) < 0.18f -> R.raw.station_mirchi  // Magic FM 106.4
            abs(frequency - 94.3f) < 0.18f -> R.raw.station_city     // MY FM 94.3
            else -> R.raw.station_generic
        }
    }

    private fun startVisualizerLoop() {
        visualizerJob?.cancel()
        visualizerJob = CoroutineScope(Dispatchers.Default).launch {
            var tick = 0f
            while (isActive) {
                delay(65)
                tick += 0.35f
                val isPlayingAudio = isStationPlaying
                val hasStation = isNearAnyStation(currentFrequency)

                val bars = if (isPlayingAudio && hasStation) {
                    val volFactor = if (isMuted) 0.05f else currentVolume.coerceIn(0.2f, 1f)
                    val isDance = abs(currentFrequency - 98.3f) < 0.2f || abs(currentFrequency - 93.5f) < 0.2f
                    val bassPulse = if (isDance) (sin(tick * 2.2f) * 0.5f + 0.5f) else (sin(tick * 1.5f) * 0.4f + 0.5f)

                    List(8) { index ->
                        val freqMultiplier = 1.0f + index * 0.4f
                        val wave = (sin(tick * freqMultiplier + index) * 0.5f + 0.5f)
                        val base = when (index) {
                            0, 1 -> 0.4f + bassPulse * 0.55f
                            2, 3 -> 0.3f + wave * 0.65f
                            4, 5 -> 0.25f + wave * 0.60f
                            else -> 0.2f + wave * 0.55f
                        }
                        val jitter = Random.nextFloat() * 0.15f
                        ((base + jitter) * volFactor).coerceIn(0.08f, 1.0f)
                    }
                } else if (isPlayingAudio) {
                    // Static flutter
                    List(8) {
                        (Random.nextFloat() * 0.18f + 0.04f).coerceIn(0.04f, 0.25f)
                    }
                } else {
                    List(8) { 0.04f }
                }
                _equalizerBars.value = bars
            }
        }
    }

    fun release() {
        visualizerJob?.cancel()
        stopStaticHiss()
        soundPool.release()
        mainScope.launch {
            radioPlayer?.release()
            radioPlayer = null
        }
    }
}

