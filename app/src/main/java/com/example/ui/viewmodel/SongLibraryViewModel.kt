package com.example.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.SongEntity
import com.example.data.repository.SongRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder {
    FAVORITES_FIRST,
    TITLE_ASC,
    TITLE_DESC,
    KEY,
    RECENT
}

class SongLibraryViewModel(
    private val repository: SongRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterFavoritesOnly = MutableStateFlow(false)
    val filterFavoritesOnly: StateFlow<Boolean> = _filterFavoritesOnly.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder.TITLE_ASC)
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateDefaultSongsIfNeeded()
        }
    }

    val songs: StateFlow<List<SongEntity>> = combine(
        repository.allSongs,
        _searchQuery,
        _filterFavoritesOnly,
        _sortOrder
    ) { allSongs, query, favoritesOnly, sort ->
        var list = allSongs

        if (favoritesOnly) {
            list = list.filter { it.isFavorite }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.title.lowercase().contains(q) ||
                it.artist.lowercase().contains(q) ||
                it.originalKey.lowercase().contains(q) ||
                it.rawChordPro.lowercase().contains(q)
            }
        }

        when (sort) {
            SortOrder.FAVORITES_FIRST -> list.sortedWith(compareByDescending<SongEntity> { it.isFavorite }.thenBy { it.title.lowercase() })
            SortOrder.TITLE_ASC -> list.sortedBy { it.title.lowercase() }
            SortOrder.TITLE_DESC -> list.sortedByDescending { it.title.lowercase() }
            SortOrder.KEY -> list.sortedWith(compareBy<SongEntity> { it.originalKey }.thenBy { it.title.lowercase() })
            SortOrder.RECENT -> list.sortedByDescending { it.updatedAt }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilterFavorites(onlyFavorites: Boolean) {
        _filterFavoritesOnly.value = onlyFavorites
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            repository.toggleFavorite(id)
        }
    }

    fun renameSong(id: Long, newTitle: String) {
        viewModelScope.launch {
            if (newTitle.isNotBlank()) {
                repository.renameSong(id, newTitle)
                _userMessage.emit("Song renamed to $newTitle")
            }
        }
    }

    fun deleteSong(id: Long) {
        viewModelScope.launch {
            repository.deleteSongById(id)
            _userMessage.emit("Song deleted")
        }
    }

    fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            val result = repository.importFromUri(uri)
            if (result.isSuccess) {
                _userMessage.emit("Imported \"${result.getOrNull()?.title}\" successfully")
            } else {
                _userMessage.emit("Failed to import: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun importFromText(rawText: String, title: String = "My Song", onComplete: ((SongEntity?) -> Unit)? = null) {
        viewModelScope.launch {
            val result = repository.importFromText(rawText, title)
            if (result.isSuccess) {
                val song = result.getOrNull()
                _userMessage.emit("Added \"${song?.title ?: title}\" to My Songs")
                onComplete?.invoke(song)
            } else {
                _userMessage.emit("Failed to add song: ${result.exceptionOrNull()?.message}")
                onComplete?.invoke(null)
            }
        }
    }

    fun updateTempoAndTimeSignature(id: Long, tempo: String, timeSignature: String) {
        viewModelScope.launch {
            val song = repository.getSongByIdDirect(id) ?: return@launch
            var updatedRaw = song.rawChordPro
            val tempoRegex = Regex("""(?m)^\{(?:tempo|bpm):[^}]*\}\r?\n?""")
            if (tempo.isNotBlank()) {
                if (tempoRegex.containsMatchIn(updatedRaw)) {
                    updatedRaw = tempoRegex.replace(updatedRaw, "{tempo: $tempo}\n")
                } else {
                    updatedRaw = "{tempo: $tempo}\n$updatedRaw"
                }
            }
            val timeRegex = Regex("""(?m)^\{(?:time|timesig|time_sig|signature|meter):[^}]*\}\r?\n?""")
            if (timeSignature.isNotBlank()) {
                if (timeRegex.containsMatchIn(updatedRaw)) {
                    updatedRaw = timeRegex.replace(updatedRaw, "{time: $timeSignature}\n")
                } else {
                    updatedRaw = "{time: $timeSignature}\n$updatedRaw"
                }
            }
            val updatedSong = song.copy(
                tempo = tempo,
                timeSignature = timeSignature,
                rawChordPro = updatedRaw,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateSong(updatedSong)
            _userMessage.emit("Updated tempo and time signature for \"${song.title}\"")
        }
    }
}
