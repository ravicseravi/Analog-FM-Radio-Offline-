package com.example.model

/**
 * Represents an FM Radio Station.
 *
 * @param frequency Frequency in MHz (between 87.5 and 108.0)
 * @param name User-friendly station name / brand (e.g. "Z100 Pop Hits")
 * @param callsign Official broadcast callsign (e.g. "WHTZ-FM")
 * @param genre Music/Programming format (e.g. "Top 40 / Pop")
 * @param city City of license / broadcast market
 * @param signalStrength Typical signal strength percentage (0-100)
 * @param rdsSample Example RDS RadioText for this station
 * @param streamUrl Live audio stream URL (MP3/AAC)
 */
data class RadioStation(
    val frequency: Float,
    val name: String,
    val callsign: String,
    val genre: String,
    val city: String = "Metro Broadcast",
    val signalStrength: Int = 92,
    val rdsSample: String = "Now Playing: Live Broadcast",
    val streamUrl: String = "",
    val fallbackUrl: String = ""
) {
    val formattedFrequency: String
        get() = String.format(java.util.Locale.US, "%.1f", frequency)
}
