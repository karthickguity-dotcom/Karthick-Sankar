package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val artist: String = "",
    val album: String = "",
    val originalKey: String = "",
    val copyright: String = "",
    val capo: Int = 0,
    val tempo: String = "",
    val timeSignature: String = "",
    val rawChordPro: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val displayArtist: String
        get() = if (artist.isNotBlank()) artist else {
            Regex("""(?i)\{(?:artist|composer|a):\s*([^}]+)\}""").find(rawChordPro)?.groupValues?.get(1)?.trim() ?: ""
        }

    val displayAlbum: String
        get() = if (album.isNotBlank()) album else {
            Regex("""(?i)\{album:\s*([^}]+)\}""").find(rawChordPro)?.groupValues?.get(1)?.trim() ?: ""
        }

    val displayKey: String
        get() = if (originalKey.isNotBlank()) originalKey else {
            Regex("""(?i)\{(?:key|k):\s*([^}]+)\}""").find(rawChordPro)?.groupValues?.get(1)?.trim() ?: ""
        }

    val displayCopyright: String
        get() = if (copyright.isNotBlank()) copyright else {
            Regex("""(?i)\{(?:copyright|copy):\s*([^}]+)\}""").find(rawChordPro)?.groupValues?.get(1)?.trim()
                ?: Regex("""(?im)^\s*(?:copyright|©)[:= ]+(.*)$""").find(rawChordPro)?.groupValues?.get(1)?.trim()
                ?: ""
        }

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
