package com.example.model

/**
 * Representation of a single audio equalizer frequency band.
 * @param index Band index (0-based)
 * @param centerFreqHz Center frequency in Hertz (e.g., 60, 230, 910, 3600, 14000)
 * @param gainDb Gain adjustment in decibels (typically -12.0 dB to +12.0 dB)
 */
data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val gainDb: Float
) {
    val formattedFreq: String
        get() = if (centerFreqHz >= 1000) {
            val kHz = centerFreqHz / 1000f
            if (kHz == kHz.toInt().toFloat()) "${kHz.toInt()} kHz" else "%.1f kHz".format(java.util.Locale.US, kHz)
        } else {
            "$centerFreqHz Hz"
        }
}

/**
 * Common acoustic sound tuning presets curated for Indian FM radio listening.
 */
enum class SoundPreset(val displayName: String, val description: String) {
    BOLLYWOOD("Bollywood Beats", "Deep resonant bass & sparkling highs for Hindi hits"),
    BASS_BOOST("Heavy Bass", "Powerful low-end punch for dance & EDM"),
    VOCAL_NEWS("Voice & News", "Clear midrange boost for talk radio & AIR commentary"),
    CLASSICAL("Classical & Ghazal", "Warm acoustics & balanced mids for vintage melodies"),
    POP_DANCE("Pop & Dance", "Crisp dynamic curve with rhythmic clarity"),
    FLAT("Flat / Acoustic", "Natural unaltered studio frequency response"),
    CUSTOM("Custom", "Personalized custom equalizer curve")
}

/**
 * Audio equalizer and acoustic DSP enhancement settings.
 */
data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val preset: SoundPreset = SoundPreset.BOLLYWOOD,
    val bassBoost: Float = 0.50f, // 0.0 to 1.0
    val virtualizer: Float = 0.35f, // 0.0 to 1.0 (Spatial 3D surround)
    val bands: List<EqualizerBand> = getDefaultBandsForPreset(SoundPreset.BOLLYWOOD)
) {
    companion object {
        fun getDefaultBandsForPreset(preset: SoundPreset): List<EqualizerBand> {
            val gains = when (preset) {
                SoundPreset.FLAT -> floatArrayOf(0f, 0f, 0f, 0f, 0f)
                SoundPreset.BOLLYWOOD -> floatArrayOf(5.5f, 3.0f, 0.5f, 3.5f, 5.0f)
                SoundPreset.BASS_BOOST -> floatArrayOf(8.0f, 6.0f, 1.0f, -1.0f, 1.0f)
                SoundPreset.VOCAL_NEWS -> floatArrayOf(-2.5f, 0.0f, 6.5f, 4.0f, -1.0f)
                SoundPreset.CLASSICAL -> floatArrayOf(3.0f, 2.0f, 3.5f, 2.5f, 2.0f)
                SoundPreset.POP_DANCE -> floatArrayOf(4.5f, 2.5f, -0.5f, 3.0f, 4.5f)
                SoundPreset.CUSTOM -> floatArrayOf(0f, 0f, 0f, 0f, 0f)
            }
            val frequencies = intArrayOf(60, 230, 910, 3600, 14000)
            return frequencies.mapIndexed { idx, freq ->
                EqualizerBand(
                    index = idx,
                    centerFreqHz = freq,
                    gainDb = gains.getOrElse(idx) { 0f }
                )
            }
        }
    }
}
