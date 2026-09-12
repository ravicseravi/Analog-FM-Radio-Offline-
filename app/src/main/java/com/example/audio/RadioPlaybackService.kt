package com.example.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaMetadata
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

private const val TAG = "RadioPlaybackService"
private const val CHANNEL_ID = "offline_fm_playback_channel"
private const val NOTIFICATION_ID = 101

/**
 * Foreground Service for continuous, uninterrupted background and lock-screen FM radio playback.
 * Holds a CPU partial WakeLock to prevent the Android OS from putting the process to sleep
 * when the screen turns off or locks.
 */
class RadioPlaybackService : Service() {

    companion object {
        const val ACTION_START_OR_UPDATE = "com.example.audio.action.START_OR_UPDATE"
        const val ACTION_PLAY = "com.example.audio.action.PLAY"
        const val ACTION_PAUSE = "com.example.audio.action.PAUSE"
        const val ACTION_TOGGLE_PLAY = "com.example.audio.action.TOGGLE_PLAY"
        const val ACTION_NEXT = "com.example.audio.action.NEXT"
        const val ACTION_PREV = "com.example.audio.action.PREV"
        const val ACTION_STOP = "com.example.audio.action.STOP"

        const val EXTRA_STATION_NAME = "extra_station_name"
        const val EXTRA_FREQUENCY = "extra_frequency"
        const val EXTRA_RDS_TEXT = "extra_rds_text"
        const val EXTRA_IS_PLAYING = "extra_is_playing"
        const val EXTRA_IS_OFFLINE = "extra_is_offline"

        private val _serviceEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
        val serviceEvents: SharedFlow<String> = _serviceEvents.asSharedFlow()

        fun updateNotification(
            context: Context,
            stationName: String,
            frequency: Float,
            rdsText: String,
            isPlaying: Boolean,
            isOffline: Boolean
        ) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_START_OR_UPDATE
                putExtra(EXTRA_STATION_NAME, stationName)
                putExtra(EXTRA_FREQUENCY, frequency)
                putExtra(EXTRA_RDS_TEXT, rdsText)
                putExtra(EXTRA_IS_PLAYING, isPlaying)
                putExtra(EXTRA_IS_OFFLINE, isOffline)
            }
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start/update RadioPlaybackService", e)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RadioPlaybackService::class.java).apply {
                action = ACTION_STOP
            }
            try {
                context.startService(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop RadioPlaybackService", e)
            }
        }
    }

    private var wakeLock: PowerManager.WakeLock? = null
    private var mediaSession: MediaSession? = null
    private var currentStationName: String = "Live FM Radio"
    private var currentFrequency: Float = 98.3f
    private var currentRdsText: String = "Broadcasting"
    private var isPlaying: Boolean = true
    private var isOffline: Boolean = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
        initMediaSession()
    }

    private fun initMediaSession() {
        try {
            mediaSession = MediaSession(this, "FMRadioMediaSession").apply {
                setCallback(object : MediaSession.Callback() {
                    override fun onPlay() {
                        _serviceEvents.tryEmit(ACTION_PLAY)
                    }

                    override fun onPause() {
                        _serviceEvents.tryEmit(ACTION_PAUSE)
                    }

                    override fun onSkipToNext() {
                        _serviceEvents.tryEmit(ACTION_NEXT)
                    }

                    override fun onSkipToPrevious() {
                        _serviceEvents.tryEmit(ACTION_PREV)
                    }

                    override fun onStop() {
                        _serviceEvents.tryEmit(ACTION_STOP)
                    }

                    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        }

                        if (keyEvent?.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_NEXT,
                                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
                                KeyEvent.KEYCODE_MEDIA_STEP_FORWARD -> {
                                    _serviceEvents.tryEmit(ACTION_NEXT)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                                KeyEvent.KEYCODE_MEDIA_REWIND,
                                KeyEvent.KEYCODE_MEDIA_STEP_BACKWARD -> {
                                    _serviceEvents.tryEmit(ACTION_PREV)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                    _serviceEvents.tryEmit(ACTION_PLAY)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                    _serviceEvents.tryEmit(ACTION_PAUSE)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                                KeyEvent.KEYCODE_HEADSETHOOK -> {
                                    _serviceEvents.tryEmit(ACTION_TOGGLE_PLAY)
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_STOP -> {
                                    _serviceEvents.tryEmit(ACTION_STOP)
                                    return true
                                }
                            }
                        }
                        return super.onMediaButtonEvent(mediaButtonIntent)
                    }
                })
                isActive = true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize MediaSession for Bluetooth/Lockscreen controls", e)
        }
    }

    private fun updateMediaSession() {
        try {
            val session = mediaSession ?: return
            val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
            val actions = PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_STOP

            val playbackState = PlaybackState.Builder()
                .setActions(actions)
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1.0f)
                .build()
            session.setPlaybackState(playbackState)

            val metadata = MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_TITLE, currentStationName)
                .putString(MediaMetadata.METADATA_KEY_ARTIST, "FM ${String.format(java.util.Locale.US, "%.1f", currentFrequency)} MHz")
                .putString(MediaMetadata.METADATA_KEY_ALBUM, currentRdsText)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, currentStationName)
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE, "FM ${String.format(java.util.Locale.US, "%.1f", currentFrequency)} MHz")
                .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, currentRdsText)
                .build()
            session.setMetadata(metadata)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating MediaSession metadata", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_STICKY

        when (action) {
            ACTION_START_OR_UPDATE -> {
                currentStationName = intent.getStringExtra(EXTRA_STATION_NAME) ?: currentStationName
                currentFrequency = intent.getFloatExtra(EXTRA_FREQUENCY, currentFrequency)
                currentRdsText = intent.getStringExtra(EXTRA_RDS_TEXT) ?: currentRdsText
                isPlaying = intent.getBooleanExtra(EXTRA_IS_PLAYING, true)
                isOffline = intent.getBooleanExtra(EXTRA_IS_OFFLINE, true)

                if (isPlaying) {
                    acquireWakeLock()
                } else {
                    releaseWakeLock()
                }

                updateMediaSession()
                val notification = buildNotification()
                startForegroundCompat(notification)
            }

            ACTION_TOGGLE_PLAY -> {
                _serviceEvents.tryEmit(ACTION_TOGGLE_PLAY)
            }

            ACTION_NEXT -> {
                _serviceEvents.tryEmit(ACTION_NEXT)
            }

            ACTION_PREV -> {
                _serviceEvents.tryEmit(ACTION_PREV)
            }

            ACTION_STOP -> {
                releaseWakeLock()
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_STICKY
    }

    private fun startForegroundCompat(notification: Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting foreground service", e)
        }
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val prevIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_PREV },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_TOGGLE_PLAY },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nextIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_NEXT },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = PendingIntent.getService(
            this,
            4,
            Intent(this, RadioPlaybackService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val modeLabel = if (isOffline) "Offline Analog FM" else "Live Stream"
        val title = "$currentStationName (${String.format(java.util.Locale.US, "%.1f", currentFrequency)} MHz)"
        val contentText = "$currentRdsText • $modeLabel"

        val playPauseIcon = if (isPlaying) {
            android.R.drawable.ic_media_pause
        } else {
            android.R.drawable.ic_media_play
        }
        val playPauseTitle = if (isPlaying) "Pause" else "Play"

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setSubText(modeLabel)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(contentIntent)
            .setOngoing(isPlaying)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(android.R.drawable.ic_media_previous, "Previous", prevIntent)
            .addAction(playPauseIcon, playPauseTitle, toggleIntent)
            .addAction(android.R.drawable.ic_media_next, "Next", nextIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setBigContentTitle(title)
                    .setSummaryText(modeLabel)
            )

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FM Radio Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing background playback controls for FM Radio"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun acquireWakeLock() {
        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "OfflineFMRadio::PlaybackWakeLock"
            ).apply {
                setReferenceCounted(false)
            }
        }
        wakeLock?.let {
            if (!it.isHeld) {
                it.acquire(24 * 60 * 60 * 1000L) // 24 hours max
                Log.d(TAG, "WakeLock acquired for background playback")
            }
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.d(TAG, "WakeLock released")
            }
        }
    }

    override fun onDestroy() {
        releaseWakeLock()
        try {
            mediaSession?.apply {
                isActive = false
                release()
            }
            mediaSession = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing MediaSession", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
