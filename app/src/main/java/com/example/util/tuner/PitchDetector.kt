package com.example.util.tuner

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.example.data.parser.AccidentalMode
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log2
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Result data of a pitch detection analysis.
 */
data class PitchResult(
    val frequency: Float,
    val noteName: String,
    val octave: Int,
    val targetFrequency: Float,
    val cents: Float,
    val clarity: Float,
    val rmsLevel: Float
) {
    val isInTune: Boolean
        get() = abs(cents) <= 4.0f

    val noteWithOctave: String
        get() = "$noteName$octave"
}

enum class TunerMode(val displayName: String) {
    CHROMATIC("Chromatic"),
    GUITAR("Guitar Standard")
}

data class GuitarStringNote(
    val stringNumber: Int,
    val noteName: String,
    val octave: Int,
    val frequency: Float
)

object PitchDetector {

    private val SHARP_NOTES = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    private val FLAT_NOTES = arrayOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    val GUITAR_STANDARD_STRINGS = listOf(
        GuitarStringNote(6, "E", 2, 82.41f),
        GuitarStringNote(5, "A", 2, 110.00f),
        GuitarStringNote(4, "D", 3, 146.83f),
        GuitarStringNote(3, "G", 3, 196.00f),
        GuitarStringNote(2, "B", 3, 246.94f),
        GuitarStringNote(1, "E", 4, 329.63f)
    )

    /**
     * Detects fundamental frequency using normalized autocorrelation with parabolic interpolation.
     *
     * @param buffer Raw 16-bit PCM audio samples
     * @param sampleRate Audio sampling rate in Hz (e.g. 22050 or 44100)
     * @param accidentalMode Note naming preference (# or b)
     * @param a4Pitch Reference pitch for A4 (default 440 Hz)
     * @return PitchResult if a confident pitch is detected, or null if below noise gate
     */
    fun detectPitch(
        buffer: ShortArray,
        sampleRate: Int,
        accidentalMode: AccidentalMode = AccidentalMode.SHARP,
        a4Pitch: Float = 440f
    ): PitchResult? {
        val size = buffer.size
        if (size < 512) return null

        // 1. Calculate RMS volume (signal energy)
        var sumSquares = 0.0
        for (i in 0 until size) {
            val sample = buffer[i].toFloat() / 32768f
            sumSquares += (sample * sample)
        }
        val rms = kotlin.math.sqrt(sumSquares / size).toFloat()

        // Noise gate: ignore silence or very quiet room noise
        if (rms < 0.008f) return null

        // 2. Autocorrelation analysis
        val minFreq = 65f   // ~C2 (covers low E on guitar ~82Hz, bass strings)
        val maxFreq = 1100f // ~C6 (well above high E on guitar ~330Hz)

        val minLag = (sampleRate / maxFreq).toInt().coerceAtLeast(2)
        val maxLag = (sampleRate / minFreq).toInt().coerceAtMost(size / 2)

        var bestLag = -1
        var bestCorrelation = -1.0f

        val r0 = calculateAutocorrelation(buffer, 0, maxLag)
        if (r0 <= 0f) return null

        val correlations = FloatArray(maxLag + 2)

        for (lag in minLag..maxLag) {
            val corr = calculateAutocorrelation(buffer, lag, maxLag)
            correlations[lag] = corr
        }

        // Peak picking: look for local maxima
        for (lag in minLag + 1 until maxLag) {
            val prev = correlations[lag - 1]
            val curr = correlations[lag]
            val next = correlations[lag + 1]

            if (curr > prev && curr > next && curr > bestCorrelation) {
                bestCorrelation = curr
                bestLag = lag
            }
        }

        if (bestLag < minLag || bestCorrelation <= 0f) return null

        val clarity = (bestCorrelation / r0).coerceIn(0f, 1f)
        if (clarity < 0.55f) return null // Below confidence threshold

        // 3. Check for octave subharmonics (prefer fundamental)
        val halfLag = bestLag / 2
        if (halfLag >= minLag && correlations[halfLag] > bestCorrelation * 0.85f) {
            bestLag = halfLag
        }

        // 4. Parabolic interpolation for sub-sample frequency accuracy
        val y1 = correlations[bestLag - 1]
        val y2 = correlations[bestLag]
        val y3 = correlations[bestLag + 1]
        val denominator = (2 * (2 * y2 - y1 - y3))
        val delta = if (denominator != 0f) (y1 - y3) / denominator else 0f
        val refinedLag = bestLag + delta

        val detectedFreq = sampleRate / refinedLag
        if (detectedFreq < minFreq || detectedFreq > maxFreq) return null

        // 5. Convert frequency to musical note, octave, and cents deviation
        val noteInfo = frequencyToNote(detectedFreq, accidentalMode, a4Pitch)

        return PitchResult(
            frequency = detectedFreq,
            noteName = noteInfo.first,
            octave = noteInfo.second,
            targetFrequency = noteInfo.third,
            cents = noteInfo.fourth,
            clarity = clarity,
            rmsLevel = rms
        )
    }

    private fun calculateAutocorrelation(buffer: ShortArray, lag: Int, windowSize: Int): Float {
        var sum = 0.0
        val end = buffer.size - lag
        val len = minOf(windowSize, end)
        for (i in 0 until len) {
            sum += (buffer[i].toDouble() * buffer[i + lag].toDouble())
        }
        return sum.toFloat()
    }

    /**
     * Converts a frequency to note name, octave, target frequency, and cents offset.
     * Returns Quad: NoteName, Octave, TargetFrequency, Cents
     */
    fun frequencyToNote(
        frequency: Float,
        accidentalMode: AccidentalMode = AccidentalMode.SHARP,
        a4Pitch: Float = 440f
    ): Quad<String, Int, Float, Float> {
        if (frequency <= 0f) return Quad("A", 4, a4Pitch, 0f)

        // MIDI formula: n = 12 * log2(f / 440) + 69
        val midiNumber = 12.0 * (ln(frequency.toDouble() / a4Pitch) / ln(2.0)) + 69.0
        val roundedMidi = midiNumber.roundToInt()

        val targetFreq = (a4Pitch * Math.pow(2.0, (roundedMidi - 69) / 12.0)).toFloat()
        val cents = (1200.0 * (ln(frequency.toDouble() / targetFreq) / ln(2.0))).toFloat()

        val noteNames = if (accidentalMode == AccidentalMode.FLAT) FLAT_NOTES else SHARP_NOTES
        val noteIndex = Math.floorMod(roundedMidi, 12)
        val octave = (roundedMidi / 12) - 1

        return Quad(noteNames[noteIndex], octave, targetFreq, cents.coerceIn(-50f, 50f))
    }
}

data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

/**
 * Pure Tone Generator using AudioTrack for ear tuning.
 */
class TonePlayer {
    private val toneLock = Any()
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    fun playTone(frequency: Float, sampleRate: Int = 44100, durationMs: Int = 2000) {
        stopTone()

        val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
        val generatedSnd = ShortArray(numSamples)

        for (i in 0 until numSamples) {
            // Apply simple attack & decay envelope to avoid speaker pops
            val env = when {
                i < 1000 -> i / 1000f
                i > numSamples - 2000 -> (numSamples - i) / 2000f
                else -> 1.0f
            }
            val angle = 2.0 * Math.PI * i / (sampleRate / frequency)
            generatedSnd[i] = (sin(angle) * 30000 * env).toInt().toShort()
        }

        synchronized(toneLock) {
            isPlaying = true
            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(generatedSnd.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack?.write(generatedSnd, 0, generatedSnd.size)
                audioTrack?.play()
            } catch (e: Throwable) {
                audioTrack = null
                isPlaying = false
            }
        }
    }

    fun stopTone() {
        synchronized(toneLock) {
            isPlaying = false
            try {
                audioTrack?.let { track ->
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        try {
                            track.stop()
                        } catch (e: Throwable) {
                            // Ignore
                        }
                    }
                    try {
                        track.release()
                    } catch (e: Throwable) {
                        // Ignore
                    }
                }
            } catch (e: Throwable) {
                // Ignore
            } finally {
                audioTrack = null
            }
        }
    }
}
