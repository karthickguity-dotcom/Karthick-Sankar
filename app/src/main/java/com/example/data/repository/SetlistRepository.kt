package com.example.data.repository

import com.example.data.local.SetlistDao
import com.example.data.local.SongDao
import com.example.data.model.SetlistEntity
import com.example.data.model.SetlistSongEntity
import com.example.data.model.SetlistSummary
import com.example.data.model.SetlistWithSongs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class SetlistRepository(
    private val setlistDao: SetlistDao,
    private val songDao: SongDao
) {
    val allSetlists: Flow<List<SetlistSummary>> = setlistDao.getAllSetlistSummaries()

    fun getSetlistById(id: Long): Flow<SetlistEntity?> = setlistDao.getSetlistById(id)

    fun getSetlistWithSongs(setlistId: Long): Flow<SetlistWithSongs?> {
        return combine(
            setlistDao.getSetlistById(setlistId),
            setlistDao.getSetlistSongsWithDetails(setlistId)
        ) { setlist, songs ->
            if (setlist == null) null
            else SetlistWithSongs(setlist = setlist, songs = songs)
        }
    }

    suspend fun getSetlistWithSongsDirect(setlistId: Long): SetlistWithSongs? = withContext(Dispatchers.IO) {
        val setlist = setlistDao.getSetlistByIdDirect(setlistId) ?: return@withContext null
        val songs = setlistDao.getSetlistSongsWithDetailsDirect(setlistId)
        SetlistWithSongs(setlist = setlist, songs = songs)
    }

    suspend fun createSetlist(
        name: String,
        description: String = "",
        eventDate: Long = System.currentTimeMillis()
    ): Long = withContext(Dispatchers.IO) {
        val entity = SetlistEntity(
            name = name.trim(),
            description = description.trim(),
            eventDate = eventDate,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        setlistDao.insertSetlist(entity)
    }

    suspend fun updateSetlist(
        id: Long,
        name: String,
        description: String = "",
        eventDate: Long = System.currentTimeMillis()
    ) = withContext(Dispatchers.IO) {
        val current = setlistDao.getSetlistByIdDirect(id) ?: return@withContext
        setlistDao.updateSetlist(
            current.copy(
                name = name.trim(),
                description = description.trim(),
                eventDate = eventDate,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteSetlist(id: Long) = withContext(Dispatchers.IO) {
        setlistDao.deleteSetlistById(id)
    }

    suspend fun addSongToSetlist(
        setlistId: Long,
        songId: Long,
        customKey: String = "",
        notes: String = ""
    ): Long = withContext(Dispatchers.IO) {
        val maxOrder = setlistDao.getMaxOrderIndex(setlistId)
        val newItem = SetlistSongEntity(
            setlistId = setlistId,
            songId = songId,
            orderIndex = maxOrder + 1,
            customKey = customKey.trim(),
            performanceNotes = notes.trim()
        )
        val insertedId = setlistDao.insertSetlistSong(newItem)
        setlistDao.touchSetlist(setlistId)
        insertedId
    }

    suspend fun addSongsToSetlist(
        setlistId: Long,
        songIds: List<Long>
    ) = withContext(Dispatchers.IO) {
        var nextOrder = setlistDao.getMaxOrderIndex(setlistId) + 1
        val items = songIds.map { songId ->
            SetlistSongEntity(
                setlistId = setlistId,
                songId = songId,
                orderIndex = nextOrder++,
                customKey = "",
                performanceNotes = ""
            )
        }
        if (items.isNotEmpty()) {
            setlistDao.insertSetlistSongs(items)
            setlistDao.touchSetlist(setlistId)
        }
    }

    suspend fun removeSongFromSetlist(itemId: Long, setlistId: Long) = withContext(Dispatchers.IO) {
        setlistDao.deleteSetlistSongById(itemId)
        setlistDao.touchSetlist(setlistId)
    }

    suspend fun reorderSongs(setlistId: Long, orderedItemIds: List<Long>) = withContext(Dispatchers.IO) {
        orderedItemIds.forEachIndexed { index, itemId ->
            setlistDao.updateSongOrder(itemId, index)
        }
        setlistDao.touchSetlist(setlistId)
    }

    suspend fun updateSongCustomKey(itemId: Long, setlistId: Long, key: String) = withContext(Dispatchers.IO) {
        setlistDao.updateCustomKey(itemId, key.trim())
        setlistDao.touchSetlist(setlistId)
    }

    suspend fun updateSongNotes(itemId: Long, setlistId: Long, notes: String) = withContext(Dispatchers.IO) {
        setlistDao.updatePerformanceNotes(itemId, notes.trim())
        setlistDao.touchSetlist(setlistId)
    }

    suspend fun duplicateSetlist(setlistId: Long, newName: String): Long = withContext(Dispatchers.IO) {
        val existingWithSongs = getSetlistWithSongsDirect(setlistId) ?: return@withContext -1L
        val newSetlistId = createSetlist(
            name = if (newName.isNotBlank()) newName else "${existingWithSongs.setlist.name} (Copy)",
            description = existingWithSongs.setlist.description,
            eventDate = System.currentTimeMillis()
        )
        val duplicateItems = existingWithSongs.songs.mapIndexed { index, song ->
            SetlistSongEntity(
                setlistId = newSetlistId,
                songId = song.songId,
                orderIndex = index,
                customKey = song.customKey,
                performanceNotes = song.performanceNotes,
                targetDurationSeconds = song.targetDurationSeconds
            )
        }
        if (duplicateItems.isNotEmpty()) {
            setlistDao.insertSetlistSongs(duplicateItems)
        }
        newSetlistId
    }

    suspend fun prepopulateDefaultSetlistIfNeeded() = withContext(Dispatchers.IO) {
        if (setlistDao.getSetlistCount() == 0 && songDao.getSongCount() > 0) {
            val defaultSetlistId = createSetlist(
                name = "Sunday Live Worship",
                description = "Morning live performance setlist"
            )
            val songs = songDao.getSongByIdDirect(1L)?.let { listOf(1L) } ?: emptyList()
            if (songs.isNotEmpty()) {
                addSongsToSetlist(defaultSetlistId, songs)
            }
        }
    }
}
