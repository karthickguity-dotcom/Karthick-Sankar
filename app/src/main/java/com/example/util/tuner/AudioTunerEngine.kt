package com.example.util.tuner

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.data.parser.AccidentalMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.abs

data class TunerUiState(
    val isListening: Boolean = false,
    val hasPermission: Boolean = false,
    val noteName: String = "--",
    val octave: Int = 0,
    val frequency: Float = 0f,
    val targetFrequency: Float = 0f,
    val cents: Float = 0f,
    val isInTune: Boolean = false,
    val clarity: Float = 0f,
    val volumeRms: Float = 0f,
    val mode: TunerMode = TunerMode.CHROMATIC,
    val selectedGuitarString: GuitarStringNote? = null,
    val accidentalMode: AccidentalMode = AccidentalMode.SHARP,
    val isPlayingTone: Boolean = false,
    val errorMessage: String? = null
)

class AudioTunerEngine(
    private val scope: CoroutineScope
) {
    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    private val audioLock = Any()
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val tonePlayer = TonePlayer()

    private val sampleRate = 22050
    private val bufferSize = 2048

    // Exponential smoothing factor for display
    private var smoothedCents = 0f

    fun setMode(mode: TunerMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            selectedGuitarString = if (mode == TunerMode.GUITAR) PitchDetector.GUITAR_STANDARD_STRINGS[0] else null
        )
    }

    fun selectGuitarString(stringNote: GuitarStringNote?) {
        _uiState.value = _uiState.value.copy(selectedGuitarString = stringNote)
    }

    fun setAccidentalMode(mode: AccidentalMode) {
        _uiState.value = _uiState.value.copy(accidentalMode = mode)
    }

    @SuppressLint("MissingPermission")
    fun startListening() {
        if (_uiState.value.isListening) return

        recordingJob?.cancel()
        recordingJob = scope.launch(Dispatchers.Default) {
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val actualBufSize = maxOf(minBuf, bufferSize * 2)

            try {
                synchronized(audioLock) {
                    audioRecord = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        actualBufSize
                    )

                    if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                        _uiState.value = _uiState.value.copy(
                            isListening = false,
                            errorMessage = "Audio recorder could not be initialized."
                        )
                        return@launch
                    }

                    audioRecord?.startRecording()
                }
                _uiState.value = _uiState.value.copy(
                    isListening = true,
                    hasPermission = true,
                    errorMessage = null
                )

                val audioBuffer = ShortArray(bufferSize)

                while (isActive && _uiState.value.isListening) {
                    val readCount = try {
                        val record = synchronized(audioLock) { audioRecord }
                        if (record != null && record.state == AudioRecord.STATE_INITIALIZED && record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                            record.read(audioBuffer, 0, bufferSize)
                        } else {
                            -1
                        }
                    } catch (e: Throwable) {
                        -1
                    }
                    if (readCount > 0 && isActive && _uiState.value.isListening) {
                        val result = PitchDetector.detectPitch(
                            buffer = audioBuffer,
                            sampleRate = sampleRate,
                            accidentalMode = _uiState.value.accidentalMode
                        )

                        if (result != null) {
                            // If in Guitar mode with target string selected:
                            val targetString = _uiState.value.selectedGuitarString
                            val finalTargetFreq = if (_uiState.value.mode == TunerMode.GUITAR && targetString != null) {
                                targetString.frequency
                            } else {
                                result.targetFrequency
                            }

                            val centsDiff = if (_uiState.value.mode == TunerMode.GUITAR && targetString != null) {
                                (1200.0 * (kotlin.math.ln(result.frequency.toDouble() / targetString.frequency) / kotlin.math.ln(2.0))).toFloat()
                                    .coerceIn(-50f, 50f)
                            } else {
                                result.cents
                            }

                            // Smooth cents to avoid jitter
                            smoothedCents = (smoothedCents * 0.4f) + (centsDiff * 0.6f)

                            val displayNote = if (_uiState.value.mode == TunerMode.GUITAR && targetString != null) {
                                targetString.noteName
                            } else {
                                result.noteName
                            }
                            val displayOctave = if (_uiState.value.mode == TunerMode.GUITAR && targetString != null) {
                                targetString.octave
                            } else {
                                result.octave
                            }

                            val inTune = abs(smoothedCents) <= 3.5f

                            _uiState.value = _uiState.value.copy(
                                noteName = displayNote,
                                octave = displayOctave,
                                frequency = result.frequency,
                                targetFrequency = finalTargetFreq,
                                cents = smoothedCents,
                                isInTune = inTune,
                                clarity = result.clarity,
                                volumeRms = result.rmsLevel
                            )
                        } else {
                            // Signal too quiet or silence: decay volume meter
                            _uiState.value = _uiState.value.copy(
                                volumeRms = (_uiState.value.volumeRms * 0.8f).coerceAtLeast(0f)
                            )
                        }
                    }
                }
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    hasPermission = false,
                    errorMessage = "Microphone permission is required for Tuner."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    errorMessage = e.message ?: "Tuner error"
                )
            } finally {
                stopRecordingInternal()
            }
        }
    }

    fun stopListening() {
        _uiState.value = _uiState.value.copy(isListening = false)
        recordingJob?.cancel()
        recordingJob = null
        stopRecordingInternal()
    }

    private fun stopRecordingInternal() {
        synchronized(audioLock) {
            try {
                audioRecord?.let { record ->
                    if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                        try {
                            record.stop()
                        } catch (e: Throwable) {
                            // Ignore
                        }
                    }
                    try {
                        record.release()
                    } catch (e: Throwable) {
                        // Ignore
                    }
                }
            } catch (e: Throwable) {
                // Ignore
            } finally {
                audioRecord = null
            }
        }
    }

    fun playReferenceTone(frequency: Float) {
        tonePlayer.playTone(frequency)
        _uiState.value = _uiState.value.copy(isPlayingTone = true)
    }

    fun stopTone() {
        tonePlayer.stopTone()
        _uiState.value = _uiState.value.copy(isPlayingTone = false)
    }

    fun release() {
        stopListening()
        stopTone()
    }
}
