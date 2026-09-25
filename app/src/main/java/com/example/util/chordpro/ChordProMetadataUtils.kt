package com.example.util.chordpro

import com.example.data.parser.ChordProParser

data class SongInfoData(
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val key: String = "",
    val copyright: String = "",
    val tempo: String = "",
    val timeSignature: String = "",
    val capo: Int = 0
)

object ChordProMetadataUtils {

    /**
     * Extracts structured [SongInfoData] from raw ChordPro text.
     */
    fun extractSongInfo(rawChordPro: String, fallbackTitle: String = "Untitled"): SongInfoData {
        val parsed = ChordProParser.parse(rawChordPro, fallbackTitle)
        return SongInfoData(
            title = parsed.title.ifBlank { fallbackTitle },
            artist = parsed.artist,
            album = parsed.album,
            key = parsed.key,
            copyright = parsed.copyright,
            tempo = parsed.tempo,
            timeSignature = parsed.timeSignature,
            capo = parsed.capo
        )
    }

    /**
     * Updates or inserts metadata directives into raw ChordPro text.
     */
    fun applySongInfo(rawChordPro: String, info: SongInfoData): String {
        var text = rawChordPro.trimStart()

        // 1. Title
        text = updateDirective(
            text = text,
            tags = listOf("title", "t"),
            canonicalTag = "title",
            newValue = info.title.trim(),
            insertAtTop = true
        )

        // 2. Artist
        text = updateDirective(
            text = text,
            tags = listOf("artist", "a", "composer"),
            canonicalTag = "artist",
            newValue = info.artist.trim(),
            insertAtTop = true
        )

        // 3. Album
        text = updateDirective(
            text = text,
            tags = listOf("album"),
            canonicalTag = "album",
            newValue = info.album.trim(),
            insertAtTop = true
        )

        // 4. Key
        text = updateDirective(
            text = text,
            tags = listOf("key", "k"),
            canonicalTag = "key",
            newValue = info.key.trim(),
            insertAtTop = true
        )

        // 5. Capo
        val capoStr = if (info.capo > 0) info.capo.toString() else ""
        text = updateDirective(
            text = text,
            tags = listOf("capo"),
            canonicalTag = "capo",
            newValue = capoStr,
            insertAtTop = true
        )

        // 6. Tempo
        text = updateDirective(
            text = text,
            tags = listOf("tempo", "bpm"),
            canonicalTag = "tempo",
            newValue = info.tempo.trim(),
            insertAtTop = true
        )

        // 7. Time Signature
        text = updateDirective(
            text = text,
            tags = listOf("time", "timesig", "time_sig", "signature", "meter"),
            canonicalTag = "time",
            newValue = info.timeSignature.trim(),
            insertAtTop = true
        )

        // 8. Copyright
        text = updateDirective(
            text = text,
            tags = listOf("copyright", "copy"),
            canonicalTag = "copyright",
            newValue = info.copyright.trim(),
            insertAtTop = true
        )

        return text
    }

    private fun updateDirective(
        text: String,
        tags: List<String>,
        canonicalTag: String,
        newValue: String,
        insertAtTop: Boolean = true
    ): String {
        val tagsPattern = tags.joinToString("|")
        val regex = Regex("""(?im)^\{\s*(?:$tagsPattern)\s*:[^}]*\}\r?\n?""")

        return if (newValue.isNotBlank()) {
            val directiveLine = "{$canonicalTag: $newValue}\n"
            if (regex.containsMatchIn(text)) {
                regex.replaceFirst(text, directiveLine)
            } else if (insertAtTop) {
                // If title directive is present at the beginning, insert after title or at very top
                val titleRegex = Regex("""(?im)^\{\s*(?:title|t)\s*:[^}]*\}\r?\n?""")
                val titleMatch = if (canonicalTag != "title") titleRegex.find(text) else null
                if (titleMatch != null && titleMatch.range.first == 0) {
                    text.substring(0, titleMatch.range.last + 1) + directiveLine + text.substring(titleMatch.range.last + 1)
                } else {
                    directiveLine + text
                }
            } else {
                "$text\n$directiveLine"
            }
        } else {
            // Remove empty directive if present
            regex.replace(text, "")
        }
    }
}
