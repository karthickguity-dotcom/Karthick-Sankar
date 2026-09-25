package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ParsedSong
import com.example.data.model.SongEntity
import com.example.data.parser.AccidentalMode
import com.example.data.parser.ChordProParser
import com.example.data.parser.Transposer
import com.example.data.repository.SongRepository
import com.example.util.metronome.MetronomeAudioEngine
import com.example.util.metronome.PracticeSessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LyricsViewModel(
    private val repository: SongRepository
) : ViewModel() {

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _parsedSong = MutableStateFlow<ParsedSong?>(null)
    val parsedSong: StateFlow<ParsedSong?> = _parsedSong.asStateFlow()

    private val _transposeSemitones = MutableStateFlow(0)
    val transposeSemitones: StateFlow<Int> = _transposeSemitones.asStateFlow()

    private val _accidentalMode = MutableStateFlow(AccidentalMode.SHARP)
    val accidentalMode: StateFlow<AccidentalMode> = _accidentalMode.asStateFlow()

    private val _isAutoScrolling = MutableStateFlow(false)
    val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling.asStateFlow()

    private val _autoScrollSpeed = MutableStateFlow(3) // 1 to 10
    val autoScrollSpeed: StateFlow<Int> = _autoScrollSpeed.asStateFlow()

    private val _isFullScreen = MutableStateFlow(false)
    val isFullScreen: StateFlow<Boolean> = _isFullScreen.asStateFlow()

    private val _showOnlyLyrics = MutableStateFlow(false)
    val showOnlyLyrics: StateFlow<Boolean> = _showOnlyLyrics.asStateFlow()

    private val _transliterationTarget = MutableStateFlow(com.example.util.transliteration.TransliterationTarget.ORIGINAL)
    val transliterationTarget: StateFlow<com.example.util.transliteration.TransliterationTarget> = _transliterationTarget.asStateFlow()

    // Metronome & Practice Session State
    private var audioEngine: MetronomeAudioEngine? = null
    private var metronomeJob: Job? = null
    private var timerJob: Job? = null
    private val tapTimestamps = mutableListOf<Long>()

    private val _practiceState = MutableStateFlow(PracticeSessionState())
    val practiceState: StateFlow<PracticeSessionState> = _practiceState.asStateFlow()

    private fun getAudioEngine(): MetronomeAudioEngine {
        if (audioEngine == null) {
            audioEngine = MetronomeAudioEngine()
        }
        return audioEngine!!
    }

    fun toggleShowOnlyLyrics() {
        _showOnlyLyrics.value = !_showOnlyLyrics.value
    }

    fun setShowOnlyLyrics(onlyLyrics: Boolean) {
        _showOnlyLyrics.value = onlyLyrics
    }

    fun setTransliterationTarget(target: com.example.util.transliteration.TransliterationTarget) {
        _transliterationTarget.value = target
    }

    fun loadSong(songId: Long, initialMode: AccidentalMode = AccidentalMode.SHARP) {
        viewModelScope.launch {
            if (_currentSong.value?.id == songId) {
                // Same song is already loaded; keep transpose intact
                _accidentalMode.value = initialMode
                return@launch
            }
            val song = repository.getSongByIdDirect(songId)
            _currentSong.value = song
            _transposeSemitones.value = 0
            _accidentalMode.value = initialMode
            _isAutoScrolling.value = false
            stopPracticeSession()

            val parsed = if (song != null) ChordProParser.parse(song.rawChordPro, song.title) else null
            _parsedSong.value = parsed

            val rawTempo = song?.tempo?.ifBlank { null } ?: parsed?.tempo?.ifBlank { null } ?: "100"
            val bpm = rawTempo.toIntOrNull() ?: 100
            val timeSig = song?.timeSignature?.ifBlank { null } ?: parsed?.timeSignature?.ifBlank { null } ?: "4/4"
            val beats = PracticeSessionState.parseBeatsPerBar(timeSig)

            _practiceState.value = PracticeSessionState(
                bpm = bpm.coerceIn(30, 280),
                timeSignature = timeSig,
                beatsPerBar = beats,
                isPlaying = false,
                isMuted = _practiceState.value.isMuted,
                relativeScrollEnabled = true,
                scrollPaceMultiplier = _practiceState.value.scrollPaceMultiplier
            )
        }
    }

    fun setAccidentalMode(mode: AccidentalMode) {
        _accidentalMode.value = mode
    }

    fun toggleAccidentalMode() {
        _accidentalMode.value = if (_accidentalMode.value == AccidentalMode.SHARP) {
            AccidentalMode.FLAT
        } else {
            AccidentalMode.SHARP
        }
    }

    fun transposeUp() {
        _transposeSemitones.value = (_transposeSemitones.value + 1)
    }

    fun transposeDown() {
        _transposeSemitones.value = (_transposeSemitones.value - 1)
    }

    fun resetTranspose() {
        _transposeSemitones.value = 0
    }

    fun toggleAutoScroll() {
        _isAutoScrolling.value = !_isAutoScrolling.value
    }

    fun setAutoScrolling(active: Boolean) {
        _isAutoScrolling.value = active
    }

    fun setAutoScrollSpeed(speed: Int) {
        _autoScrollSpeed.value = speed.coerceIn(1, 10)
    }

    fun toggleFullScreen() {
        _isFullScreen.value = !_isFullScreen.value
    }

    fun getCurrentKey(): String {
        val original = _currentSong.value?.originalKey?.ifBlank { null }
            ?: _parsedSong.value?.key?.ifBlank { null }
            ?: findFirstChord()
            ?: ""
        if (original.isBlank()) return ""
        return Transposer.transposeKey(original, _transposeSemitones.value, _accidentalMode.value)
    }

    private fun findFirstChord(): String? {
        val sections = _parsedSong.value?.sections ?: return null
        for (section in sections) {
            for (line in section.lines) {
                for (pair in line.pairs) {
                    val chord = pair.chord?.trim()
                    if (!chord.isNullOrBlank()) {
                        return chord
                    }
                }
            }
        }
        return null
    }

    fun updateTempoAndTimeSignature(newTempo: String, newTimeSignature: String) {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            var updatedRaw = song.rawChordPro
            val tempoRegex = Regex("""(?m)^\{(?:tempo|bpm):[^}]*\}\r?\n?""")
            if (newTempo.isNotBlank()) {
                if (tempoRegex.containsMatchIn(updatedRaw)) {
                    updatedRaw = tempoRegex.replace(updatedRaw, "{tempo: $newTempo}\n")
                } else {
                    updatedRaw = "{tempo: $newTempo}\n$updatedRaw"
                }
            }
            val timeRegex = Regex("""(?m)^\{(?:time|timesig|time_sig|signature|meter):[^}]*\}\r?\n?""")
            if (newTimeSignature.isNotBlank()) {
                if (timeRegex.containsMatchIn(updatedRaw)) {
                    updatedRaw = timeRegex.replace(updatedRaw, "{time: $newTimeSignature}\n")
                } else {
                    updatedRaw = "{time: $newTimeSignature}\n$updatedRaw"
                }
            }
            val updatedSong = song.copy(
                tempo = newTempo,
                timeSignature = newTimeSignature,
                rawChordPro = updatedRaw,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateSong(updatedSong)
            _currentSong.value = updatedSong
            _parsedSong.value = ChordProParser.parse(updatedRaw, updatedSong.title)
        }
    }

    fun updateSongMetadata(
        title: String,
        artist: String,
        album: String,
        key: String,
        copyright: String,
        tempo: String,
        timeSignature: String,
        capo: Int
    ) {
        val song = _currentSong.value ?: return
        viewModelScope.launch {
            val info = com.example.util.chordpro.SongInfoData(
                title = title.ifBlank { song.title },
                artist = artist,
                album = album,
                key = key,
                copyright = copyright,
                tempo = tempo,
                timeSignature = timeSignature,
                capo = capo
            )
            val updatedRaw = com.example.util.chordpro.ChordProMetadataUtils.applySongInfo(song.rawChordPro, info)
            val updatedSong = song.copy(
                title = title.ifBlank { song.title },
                artist = artist,
                album = album,
                originalKey = key,
                copyright = copyright,
                tempo = tempo,
                timeSignature = timeSignature,
                capo = capo,
                rawChordPro = updatedRaw,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateSong(updatedSong)
            _currentSong.value = updatedSong
            _parsedSong.value = ChordProParser.parse(updatedRaw, updatedSong.title)
        }
    }

    // ==========================================
    // METRONOME & PRACTICE SESSION CONTROLS
    // ==========================================

    fun togglePracticeSession() {
        if (_practiceState.value.isPlaying) {
            stopPracticeSession()
        } else {
            startPracticeSession()
        }
    }

    fun startPracticeSession() {
        _practiceState.update { it.copy(isPlaying = true) }

        metronomeJob?.cancel()
        timerJob?.cancel()

        metronomeJob = viewModelScope.launch(Dispatchers.Default) {
            val engine = getAudioEngine()
            engine.isMuted = _practiceState.value.isMuted

            // Count-in phase if enabled
            val countInBars = _practiceState.value.countInBars
            if (countInBars > 0) {
                val totalBeats = countInBars * _practiceState.value.beatsPerBar
                _practiceState.update {
                    it.copy(
                        isCountingIn = true,
                        countInBeat = 1,
                        countInTotalBeats = totalBeats
                    )
                }
                for (b in 1..totalBeats) {
                    if (!isActive || !_practiceState.value.isPlaying) return@launch
                    _practiceState.update { it.copy(countInBeat = b) }
                    val isAccented = ((b - 1) % _practiceState.value.beatsPerBar == 0)
                    engine.playBeat(isAccented)
                    val beatIntervalMs = (60_000L / _practiceState.value.bpm.coerceAtLeast(30))
                    delay(beatIntervalMs)
                }
            }

            _practiceState.update {
                it.copy(isCountingIn = false, currentBar = 1, currentBeat = 1)
            }

            var bar = 1
            var beat = 1
            var nextTickNano = System.nanoTime()

            while (isActive && _practiceState.value.isPlaying) {
                val currentBpm = _practiceState.value.bpm.coerceAtLeast(30)
                val beatsInBar = _practiceState.value.beatsPerBar
                val intervalNano = (60_000_000_000L / currentBpm)

                val isDownbeat = (beat == 1)
                engine.playBeat(isDownbeat)

                _practiceState.update {
                    it.copy(currentBar = bar, currentBeat = beat)
                }

                beat++
                if (beat > beatsInBar) {
                    beat = 1
                    bar++
                }

                nextTickNano += intervalNano
                val sleepNano = nextTickNano - System.nanoTime()
                val sleepMs = sleepNano / 1_000_000L
                if (sleepMs > 0) {
                    delay(sleepMs)
                } else {
                    nextTickNano = System.nanoTime()
                    delay(5)
                }
            }
        }

        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                if (_practiceState.value.isPlaying && !_practiceState.value.isCountingIn) {
                    _practiceState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                }
            }
        }
    }

    fun stopPracticeSession() {
        metronomeJob?.cancel()
        metronomeJob = null
        timerJob?.cancel()
        timerJob = null
        _practiceState.update { it.copy(isPlaying = false, isCountingIn = false) }
    }

    fun resetPracticeSession() {
        stopPracticeSession()
        _practiceState.update {
            it.copy(
                isPlaying = false,
                isCountingIn = false,
                currentBar = 1,
                currentBeat = 1,
                elapsedSeconds = 0
            )
        }
    }

    fun setPracticeBpm(bpm: Int) {
        val clamped = bpm.coerceIn(30, 280)
        _practiceState.update { it.copy(bpm = clamped) }
    }

    fun adjustPracticeBpm(delta: Int) {
        val newBpm = (_practiceState.value.bpm + delta).coerceIn(30, 280)
        _practiceState.update { it.copy(bpm = newBpm) }
    }

    fun setPracticeTimeSignature(timeSig: String) {
        val beats = PracticeSessionState.parseBeatsPerBar(timeSig)
        _practiceState.update {
            it.copy(
                timeSignature = timeSig,
                beatsPerBar = beats,
                currentBeat = 1
            )
        }
    }

    fun togglePracticeMute() {
        val next = !_practiceState.value.isMuted
        _practiceState.update { it.copy(isMuted = next) }
        audioEngine?.isMuted = next
    }

    fun togglePracticeRelativeScroll() {
        _practiceState.update { it.copy(relativeScrollEnabled = !it.relativeScrollEnabled) }
    }

    fun setPracticeScrollPace(multiplier: Float) {
        _practiceState.update { it.copy(scrollPaceMultiplier = multiplier.coerceIn(0.3f, 3.0f)) }
    }

    fun setPracticeCountInBars(bars: Int) {
        _practiceState.update { it.copy(countInBars = bars.coerceIn(0, 4)) }
    }

    fun registerPracticeTap() {
        val now = System.currentTimeMillis()
        if (tapTimestamps.isNotEmpty() && now - tapTimestamps.last() > 2500) {
            tapTimestamps.clear()
        }
        tapTimestamps.add(now)
        if (tapTimestamps.size > 6) {
            tapTimestamps.removeAt(0)
        }
        if (tapTimestamps.size >= 2) {
            val intervals = tapTimestamps.zipWithNext { a, b -> b - a }
            val avgMs = intervals.average()
            if (avgMs > 0) {
                val bpm = (60000.0 / avgMs).toInt().coerceIn(30, 280)
                setPracticeBpm(bpm)
            }
        }
    }

    fun savePracticeBpmToSong() {
        val bpmStr = _practiceState.value.bpm.toString()
        val timeSig = _practiceState.value.timeSignature
        updateTempoAndTimeSignature(bpmStr, timeSig)
    }

    override fun onCleared() {
        super.onCleared()
        stopPracticeSession()
        audioEngine?.release()
        audioEngine = null
    }
}
