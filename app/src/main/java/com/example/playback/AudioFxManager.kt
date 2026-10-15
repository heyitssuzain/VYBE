package com.example.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class EqualizerPreset(val displayName: String) {
    FLAT("Flat / Pure"),
    BASS_BOOST("Cosmic Bass"),
    VOCAL("Stellar Vocal"),
    ELECTRONIC("Cyber Electronic"),
    AMBIENT("Deep Nebula")
}

class AudioFxManager {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private val _isSupported = MutableStateFlow(false)
    val isSupported: StateFlow<Boolean> = _isSupported.asStateFlow()

    private val _currentPreset = MutableStateFlow(EqualizerPreset.FLAT)
    val currentPreset: StateFlow<EqualizerPreset> = _currentPreset.asStateFlow()

    private val _bassStrength = MutableStateFlow(0)
    val bassStrength: StateFlow<Int> = _bassStrength.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        if (audioSessionId <= 0) return
        try {
            release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = true
            }
            _isSupported.value = true
            applyPreset(_currentPreset.value)
        } catch (e: Exception) {
            _isSupported.value = false
        }
    }

    fun applyPreset(preset: EqualizerPreset) {
        _currentPreset.value = preset
        val eq = equalizer ?: return
        try {
            val numBands = eq.numberOfBands.toInt()
            val minEqLevel = eq.bandLevelRange[0]
            val maxEqLevel = eq.bandLevelRange[1]
            val midEqLevel = 0.toShort()

            when (preset) {
                EqualizerPreset.FLAT -> {
                    for (i in 0 until numBands) {
                        eq.setBandLevel(i.toShort(), midEqLevel)
                    }
                    setBassStrength(0)
                }
                EqualizerPreset.BASS_BOOST -> {
                    if (numBands > 0) eq.setBandLevel(0, maxEqLevel)
                    if (numBands > 1) eq.setBandLevel(1, (maxEqLevel / 2).toShort())
                    setBassStrength(800)
                }
                EqualizerPreset.VOCAL -> {
                    val mid = numBands / 2
                    for (i in 0 until numBands) {
                        val level = if (i == mid || i == mid - 1) (maxEqLevel * 0.7f).toInt().toShort() else midEqLevel
                        eq.setBandLevel(i.toShort(), level)
                    }
                    setBassStrength(200)
                }
                EqualizerPreset.ELECTRONIC -> {
                    if (numBands > 0) eq.setBandLevel(0, (maxEqLevel * 0.8f).toInt().toShort())
                    if (numBands > numBands - 1) eq.setBandLevel((numBands - 1).toShort(), (maxEqLevel * 0.9f).toInt().toShort())
                    setBassStrength(600)
                }
                EqualizerPreset.AMBIENT -> {
                    for (i in 0 until numBands) {
                        val level = if (i < numBands / 2) (maxEqLevel * 0.3f).toInt().toShort() else (maxEqLevel * 0.6f).toInt().toShort()
                        eq.setBandLevel(i.toShort(), level)
                    }
                    setBassStrength(400)
                }
            }
        } catch (e: Exception) {
            // Ignore hardware constraints
        }
    }

    fun setBassStrength(strength: Int) {
        _bassStrength.value = strength.coerceIn(0, 1000)
        try {
            bassBoost?.let {
                if (it.strengthSupported) {
                    it.setStrength(_bassStrength.value.toShort())
                }
            }
        } catch (e: Exception) {}
    }

    fun release() {
        try { equalizer?.release() } catch (e: Exception) {}
        try { bassBoost?.release() } catch (e: Exception) {}
        equalizer = null
        bassBoost = null
    }
}
