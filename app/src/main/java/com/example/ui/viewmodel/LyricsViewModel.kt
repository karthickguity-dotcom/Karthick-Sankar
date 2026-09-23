package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ParsedSong
import com.example.data.model.SongEntity
import com.example.data.parser.AccidentalMode
import com.example.data.parser.ChordProParser
import com.example.data.parser.Transposer
import com.example.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            if (song != null) {
                _parsedSong.value = ChordProParser.parse(song.rawChordPro, song.title)
            }
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
}
