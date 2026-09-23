package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "",
    val originalKey: String = "",
    val capo: Int = 0,
    val tempo: String = "",
    val timeSignature: String = "",
    val rawChordPro: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayTempo: String
        get() = if (tempo.isNotBlank()) tempo else {
            Regex("""(?i)\{(?:tempo|bpm):\s*([^}]+)\}""").find(rawChordPro)?.groupValues?.get(1)?.trim()
                ?: Regex("""(?im)^\s*(?:tempo|bpm)[:= ]+(\d+)\b""").find(rawChordPro)?.groupValues?.get(1)?.trim()
                ?: ""
        }

    val displayTimeSignature: String
        get() = if (timeSignature.isNotBlank()) timeSignature else {
            Regex("""(?i)\{(?:time|timesig|time_sig|signature|meter):\s*([^}]+)\}""").find(rawChordPro)?.groupValues?.get(1)?.trim()
                ?: Regex("""(?im)^\s*(?:time|timesig|signature|meter)[:= ]+([1-9]\d?/[1-9]\d?)\b""").find(rawChordPro)?.groupValues?.get(1)?.trim()
                ?: ""
        }
}
