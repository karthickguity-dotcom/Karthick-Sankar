package com.example.util.metronome

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlin.math.exp
import kotlin.math.sin

/**
 * High-performance, zero-latency synthesizer for audible metronome clicks.
 * Uses pre-rendered 16-bit PCM mono samples in static AudioTracks.
 * Produces crisp woodblock-style accented clicks on Beat 1 and standard clicks on other beats.
 */
class MetronomeAudioEngine {

    private var accentTrack: AudioTrack? = null
    private var normalTrack: AudioTrack? = null
    private var subTrack: AudioTrack? = null

    private var isInitialized = false
    var isMuted: Boolean = false
    var volume: Float = 0.85f
        set(value) {
            field = value.coerceIn(0f, 1f)
            try {
                accentTrack?.setVolume(field)
                normalTrack?.setVolume(field * 0.8f)
                subTrack?.setVolume(field * 0.6f)
            } catch (_: Throwable) {}
        }

    init {
        initializeTracks()
    }

    private fun initializeTracks() {
        try {
            val sampleRate = 44100
            // Accented beat 1 click: 1600 Hz with rich woodblock timbre
            val accentPcm = generateClickPcm(
                freq1 = 1550.0,
                freq2 = 2480.0,
                durationMs = 28,
                sampleRate = sampleRate,
                volume = 0.95f
            )
            // Standard beat click: 920 Hz
            val normalPcm = generateClickPcm(
                freq1 = 920.0,
                freq2 = 1472.0,
                durationMs = 22,
                sampleRate = sampleRate,
                volume = 0.75f
            )
            // Sub-beat click (if used for count-in or subdivisions): 1200 Hz
            val subPcm = generateClickPcm(
                freq1 = 1200.0,
                freq2 = 1920.0,
                durationMs = 18,
                sampleRate = sampleRate,
                volume = 0.55f
            )

            accentTrack = createStaticTrack(accentPcm, sampleRate)
            normalTrack = createStaticTrack(normalPcm, sampleRate)
            subTrack = createStaticTrack(subPcm, sampleRate)

            isInitialized = accentTrack != null && normalTrack != null
        } catch (e: Throwable) {
            Log.w("MetronomeAudioEngine", "AudioTrack init skipped or failed: ${e.message}")
            isInitialized = false
        }
    }

    private fun createStaticTrack(pcmData: ByteArray, sampleRate: Int): AudioTrack? {
        return try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(pcmData.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(pcmData, 0, pcmData.size)
            track.setVolume(volume)
            track
        } catch (e: Throwable) {
            null
        }
    }

    /**
     * Plays a click sound for the current beat.
     * @param isAccented true for downbeat (Beat 1), false for other beats.
     */
    fun playBeat(isAccented: Boolean) {
        if (isMuted || !isInitialized) return
        try {
            val track = if (isAccented) accentTrack else normalTrack
            track?.let {
                it.stop()
                it.reloadStaticData()
                it.play()
            }
        } catch (e: Throwable) {
            Log.w("MetronomeAudioEngine", "Error playing click: ${e.message}")
        }
    }

    /**
     * Plays a subtle count-in tick.
     */
    fun playCountInTick() {
        if (isMuted || !isInitialized) return
        try {
            val track = subTrack ?: accentTrack
            track?.let {
                it.stop()
                it.reloadStaticData()
                it.play()
            }
        } catch (_: Throwable) {}
    }

    fun release() {
        try {
            accentTrack?.stop()
            accentTrack?.release()
            accentTrack = null

            normalTrack?.stop()
            normalTrack?.release()
            normalTrack = null

            subTrack?.stop()
            subTrack?.release()
            subTrack = null

            isInitialized = false
        } catch (_: Throwable) {}
    }

    companion object {
        /**
         * Synthesize short 16-bit PCM mono impulse with exponential decay and two harmonic components
         * to emulate an authentic acoustic metronome woodblock / rim click.
         */
        fun generateClickPcm(
            freq1: Double,
            freq2: Double,
            durationMs: Int,
            sampleRate: Int = 44100,
            volume: Float = 0.8f
        ): ByteArray {
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ByteArray(numSamples * 2)
            val decayTau = (durationMs * 0.35) / 1000.0

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = exp(-t / decayTau)
                val signal = (sin(2.0 * Math.PI * freq1 * t) * 0.75 +
                              sin(2.0 * Math.PI * freq2 * t) * 0.25) * decay * volume
                val pcmValue = (signal.coerceIn(-1.0, 1.0) * 32767.0).toInt().toShort()
                buffer[i * 2] = (pcmValue.toInt() and 0xFF).toByte()
                buffer[i * 2 + 1] = ((pcmValue.toInt() shr 8) and 0xFF).toByte()
            }
            return buffer
        }
    }
}
