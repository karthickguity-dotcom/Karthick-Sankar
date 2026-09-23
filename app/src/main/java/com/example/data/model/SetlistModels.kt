package com.example.data.model

data class SetlistSongWithDetails(
    val itemId: Long,
    val setlistId: Long,
    val songId: Long,
    val orderIndex: Int,
    val customKey: String,
    val performanceNotes: String,
    val targetDurationSeconds: Int,
    val title: String,
    val artist: String,
    val originalKey: String,
    val capo: Int,
    val tempo: String,
    val timeSignature: String,
    val rawChordPro: String
)

data class SetlistWithSongs(
    val setlist: SetlistEntity,
    val songs: List<SetlistSongWithDetails>
)

data class SetlistSummary(
    val id: Long,
    val name: String,
    val description: String,
    val eventDate: Long,
    val songCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)
