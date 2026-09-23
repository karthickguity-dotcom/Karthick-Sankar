package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SetlistSongWithDetails
import com.example.data.model.SetlistSummary
import com.example.data.model.SetlistWithSongs
import com.example.data.repository.SetlistRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SetlistViewModel(
    private val setlistRepository: SetlistRepository
) : ViewModel() {

    val setlists: StateFlow<List<SetlistSummary>> = setlistRepository.allSetlists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedSetlistId = MutableStateFlow<Long?>(null)
    val selectedSetlistId: StateFlow<Long?> = _selectedSetlistId.asStateFlow()

    val selectedSetlist: StateFlow<SetlistWithSongs?> = _selectedSetlistId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else setlistRepository.getSetlistWithSongs(id)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Live Performance Mode Navigation
    private val _activeSongIndex = MutableStateFlow(0)
    val activeSongIndex: StateFlow<Int> = _activeSongIndex.asStateFlow()

    val activeSong: StateFlow<SetlistSongWithDetails?> = combine(
        selectedSetlist,
        _activeSongIndex
    ) { setlist, index ->
        setlist?.songs?.getOrNull(index)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val hasPreviousSong: StateFlow<Boolean> = combine(
        selectedSetlist,
        _activeSongIndex
    ) { setlist, index ->
        index > 0 && setlist != null && setlist.songs.isNotEmpty()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val hasNextSong: StateFlow<Boolean> = combine(
        selectedSetlist,
        _activeSongIndex
    ) { setlist, index ->
        setlist != null && index < setlist.songs.size - 1
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            setlistRepository.prepopulateDefaultSetlistIfNeeded()
        }
    }

    fun selectSetlist(id: Long) {
        _selectedSetlistId.value = id
        _activeSongIndex.value = 0
    }

    fun clearSelectedSetlist() {
        _selectedSetlistId.value = null
        _activeSongIndex.value = 0
    }

    fun createSetlist(name: String, description: String = "") {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessage.emit("Setlist name cannot be empty")
                return@launch
            }
            val newId = setlistRepository.createSetlist(name, description)
            _selectedSetlistId.value = newId
            _activeSongIndex.value = 0
            _userMessage.emit("Created setlist \"$name\"")
        }
    }

    fun updateSetlist(id: Long, name: String, description: String = "") {
        viewModelScope.launch {
            if (name.isBlank()) {
                _userMessage.emit("Setlist name cannot be empty")
                return@launch
            }
            setlistRepository.updateSetlist(id, name, description)
            _userMessage.emit("Setlist updated")
        }
    }

    fun deleteSetlist(id: Long) {
        viewModelScope.launch {
            if (_selectedSetlistId.value == id) {
                _selectedSetlistId.value = null
                _activeSongIndex.value = 0
            }
            setlistRepository.deleteSetlist(id)
            _userMessage.emit("Setlist deleted")
        }
    }

    fun addSongToSetlist(setlistId: Long, songId: Long, customKey: String = "", notes: String = "") {
        viewModelScope.launch {
            setlistRepository.addSongToSetlist(setlistId, songId, customKey, notes)
            _userMessage.emit("Song added to setlist")
        }
    }

    fun addMultipleSongsToSetlist(setlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            setlistRepository.addSongsToSetlist(setlistId, songIds)
            _userMessage.emit("${songIds.size} songs added to setlist")
        }
    }

    fun removeSongFromSetlist(itemId: Long, setlistId: Long) {
        viewModelScope.launch {
            setlistRepository.removeSongFromSetlist(itemId, setlistId)
            val currentSongs = selectedSetlist.value?.songs ?: emptyList()
            if (_activeSongIndex.value >= currentSongs.size - 1 && _activeSongIndex.value > 0) {
                _activeSongIndex.value = _activeSongIndex.value - 1
            }
            _userMessage.emit("Song removed from setlist")
        }
    }

    fun moveSongUp(setlistId: Long, itemIndex: Int) {
        val songs = selectedSetlist.value?.songs ?: return
        if (itemIndex <= 0 || itemIndex >= songs.size) return

        val reordered = songs.toMutableList()
        val item = reordered.removeAt(itemIndex)
        reordered.add(itemIndex - 1, item)

        viewModelScope.launch {
            setlistRepository.reorderSongs(setlistId, reordered.map { it.itemId })
        }
    }

    fun moveSongDown(setlistId: Long, itemIndex: Int) {
        val songs = selectedSetlist.value?.songs ?: return
        if (itemIndex < 0 || itemIndex >= songs.size - 1) return

        val reordered = songs.toMutableList()
        val item = reordered.removeAt(itemIndex)
        reordered.add(itemIndex + 1, item)

        viewModelScope.launch {
            setlistRepository.reorderSongs(setlistId, reordered.map { it.itemId })
        }
    }

    fun updatePerformanceNotes(itemId: Long, setlistId: Long, notes: String) {
        viewModelScope.launch {
            setlistRepository.updateSongNotes(itemId, setlistId, notes)
            _userMessage.emit("Notes saved")
        }
    }

    fun updateCustomKey(itemId: Long, setlistId: Long, key: String) {
        viewModelScope.launch {
            setlistRepository.updateSongCustomKey(itemId, setlistId, key)
            _userMessage.emit("Transposed key saved for setlist")
        }
    }

    fun duplicateSetlist(setlistId: Long, newName: String = "") {
        viewModelScope.launch {
            val copyId = setlistRepository.duplicateSetlist(setlistId, newName)
            if (copyId > 0) {
                _selectedSetlistId.value = copyId
                _activeSongIndex.value = 0
                _userMessage.emit("Setlist duplicated")
            }
        }
    }

    // Live performance control methods
    fun nextSong() {
        val songs = selectedSetlist.value?.songs ?: return
        if (_activeSongIndex.value < songs.size - 1) {
            _activeSongIndex.value = _activeSongIndex.value + 1
        }
    }

    fun previousSong() {
        if (_activeSongIndex.value > 0) {
            _activeSongIndex.value = _activeSongIndex.value - 1
        }
    }

    fun jumpToSong(index: Int) {
        val songs = selectedSetlist.value?.songs ?: return
        if (index in songs.indices) {
            _activeSongIndex.value = index
        }
    }
}
