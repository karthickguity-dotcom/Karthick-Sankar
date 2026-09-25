package com.example.data.parser

import com.example.data.model.ChordLyricPair
import com.example.data.model.ChordProLine
import com.example.data.model.LineType
import com.example.data.model.ParsedSong
import com.example.data.model.SectionType
import com.example.data.model.SongSection

object ChordProParser {

    private val DIRECTIVE_REGEX = Regex("^\\s*\\{\\s*([a-zA-Z0-9_]+)(?:\\s*:\\s*(.*?))?\\s*\\}\\s*$")
    private val CHORD_REGEX = Regex("\\[([^\\]]+)\\]")

    /**
     * Parses a raw ChordPro or text string into a structured [ParsedSong].
     */
    fun parse(rawText: String, defaultTitle: String = "Untitled Song"): ParsedSong {
        var title = ""
        var artist = ""
        var album = ""
        var key = ""
        var copyright = ""
        var capo = 0
        var tempo = ""
        var timeSignature = ""

        val sections = mutableListOf<SongSection>()
        var currentSectionTitle = ""
        var currentSectionType = SectionType.GENERAL
        var currentLines = mutableListOf<ChordProLine>()

        fun flushSection() {
            if (currentLines.isNotEmpty() || currentSectionTitle.isNotEmpty()) {
                sections.add(
                    SongSection(
                        title = currentSectionTitle,
                        type = currentSectionType,
                        lines = currentLines.toList()
                    )
                )
                currentLines.clear()
                currentSectionTitle = ""
                currentSectionType = SectionType.GENERAL
            }
        }

        val lines = rawText.lines()

        for (rawLine in lines) {
            val line = rawLine.trimEnd()

            // 1. Check for ChordPro directive: {tag: value} or {tag}
            val directiveMatch = DIRECTIVE_REGEX.find(line.trim())
            if (directiveMatch != null) {
                val tag = directiveMatch.groupValues[1].lowercase()
                val value = directiveMatch.groupValues.getOrNull(2)?.trim() ?: ""

                when (tag) {
                    "title", "t" -> if (title.isBlank()) title = value
                    "artist", "a", "composer" -> if (artist.isBlank()) artist = value
                    "album" -> if (album.isBlank()) album = value
                    "key", "k" -> if (key.isBlank()) key = value
                    "copyright", "copy" -> if (copyright.isBlank()) copyright = value
                    "capo" -> capo = value.toIntOrNull() ?: 0
                    "tempo", "bpm" -> if (tempo.isBlank()) tempo = value
                    "time", "timesig", "time_sig", "signature", "meter" -> if (timeSignature.isBlank()) timeSignature = value
                    "subtitle", "st" -> {
                        if (artist.isBlank()) artist = value
                        else currentLines.add(ChordProLine(LineType.COMMENT, text = value))
                    }
                    "comment", "c", "ci" -> {
                        currentLines.add(ChordProLine(LineType.COMMENT, text = value))
                    }
                    "start_of_chorus", "soc" -> {
                        flushSection()
                        currentSectionTitle = if (value.isNotBlank()) value else "Chorus"
                        currentSectionType = SectionType.CHORUS
                    }
                    "end_of_chorus", "eoc" -> {
                        flushSection()
                    }
                    "start_of_verse", "sov" -> {
                        flushSection()
                        currentSectionTitle = if (value.isNotBlank()) value else "Verse"
                        currentSectionType = SectionType.VERSE
                    }
                    "end_of_verse", "eov" -> {
                        flushSection()
                    }
                    "start_of_bridge", "sob" -> {
                        flushSection()
                        currentSectionTitle = if (value.isNotBlank()) value else "Bridge"
                        currentSectionType = SectionType.BRIDGE
                    }
                    "end_of_bridge", "eob" -> {
                        flushSection()
                    }
                    "verse" -> {
                        flushSection()
                        currentSectionTitle = value.ifBlank { "Verse" }
                        currentSectionType = SectionType.VERSE
                    }
                    "chorus" -> {
                        flushSection()
                        currentSectionTitle = value.ifBlank { "Chorus" }
                        currentSectionType = SectionType.CHORUS
                    }
                    "bridge" -> {
                        flushSection()
                        currentSectionTitle = value.ifBlank { "Bridge" }
                        currentSectionType = SectionType.BRIDGE
                    }
                    else -> {
                        // Unrecognized directive: preserve as comment if it had text
                        if (value.isNotBlank()) {
                            currentLines.add(ChordProLine(LineType.COMMENT, text = "$tag: $value"))
                        }
                    }
                }
                continue
            }

            // 2. Check for blank lines
            if (line.isBlank()) {
                currentLines.add(ChordProLine(LineType.BLANK))
                continue
            }

            // 3. Check for standalone Section headers in plain text, e.g. "Verse 1:", "[Chorus]", "Chorus:"
            val plainHeaderMatch = Regex("^(?:\\[)?(Verse\\s*\\d*|Chorus|Bridge|Intro|Outro|Pre-Chorus|Ending)(?:\\])?\\s*:?$", RegexOption.IGNORE_CASE)
                .find(line.trim())
            if (plainHeaderMatch != null) {
                val headerName = plainHeaderMatch.groupValues[1]
                flushSection()
                currentSectionTitle = headerName
                currentSectionType = when {
                    headerName.startsWith("chorus", ignoreCase = true) -> SectionType.CHORUS
                    headerName.startsWith("verse", ignoreCase = true) -> SectionType.VERSE
                    headerName.startsWith("bridge", ignoreCase = true) -> SectionType.BRIDGE
                    headerName.startsWith("intro", ignoreCase = true) -> SectionType.INTRO
                    headerName.startsWith("outro", ignoreCase = true) -> SectionType.OUTRO
                    headerName.startsWith("pre", ignoreCase = true) -> SectionType.PRE_CHORUS
                    else -> SectionType.GENERAL
                }
                continue
            }

            // 4. Parse inline chords [Chord]Lyrics
            if (line.contains("[") && line.contains("]")) {
                val pairs = parseChordLyricLine(line)
                currentLines.add(ChordProLine(LineType.CHORD_LYRIC, pairs = pairs))
            } else {
                // Line without chords: preserve full lyrics as written (Unicode safe)
                currentLines.add(
                    ChordProLine(
                        LineType.CHORD_LYRIC,
                        pairs = listOf(ChordLyricPair(chord = null, lyric = line))
                    )
                )
            }
        }

        flushSection()

        // Fallback for title: if no title found in directives, search for first non-empty line
        val finalTitle = when {
            title.isNotBlank() -> title
            defaultTitle.isNotBlank() -> defaultTitle
            else -> "Untitled Song"
        }

        // Infer key if not specified: scan first chord
        var detectedKey = key
        if (detectedKey.isBlank()) {
            for (section in sections) {
                for (line in section.lines) {
                    val firstChord = line.pairs.firstOrNull { it.chord != null }?.chord
                    if (firstChord != null) {
                        detectedKey = firstChord.replace(Regex("/.*"), "")
                        break
                    }
                }
                if (detectedKey.isNotBlank()) break
            }
        }

        return ParsedSong(
            title = finalTitle,
            artist = artist,
            album = album,
            key = detectedKey,
            copyright = copyright,
            capo = capo,
            tempo = tempo,
            timeSignature = timeSignature,
            sections = sections
        )
    }

    /**
     * Splits a line like "[G]Amazing [C]grace how [G]sweet" into structured ChordLyricPairs.
     */
    fun parseChordLyricLine(line: String): List<ChordLyricPair> {
        val pairs = mutableListOf<ChordLyricPair>()
        val chordMatches = CHORD_REGEX.findAll(line).toList()

        if (chordMatches.isEmpty()) {
            return listOf(ChordLyricPair(chord = null, lyric = line))
        }

        // Check if there are lyric characters before the first chord
        val firstMatch = chordMatches.first()
        if (firstMatch.range.first > 0) {
            val initialLyric = line.substring(0, firstMatch.range.first)
            pairs.add(ChordLyricPair(chord = null, lyric = initialLyric))
        }

        for (i in chordMatches.indices) {
            val match = chordMatches[i]
            val chord = match.groupValues[1]

            val lyricStart = match.range.last + 1
            val lyricEnd = if (i + 1 < chordMatches.size) {
                chordMatches[i + 1].range.first
            } else {
                line.length
            }

            val lyric = if (lyricStart <= lyricEnd && lyricStart < line.length) {
                line.substring(lyricStart, lyricEnd)
            } else {
                ""
            }

            pairs.add(ChordLyricPair(chord = chord, lyric = lyric))
        }

        return pairs
    }

    /**
     * Reconstructs raw ChordPro string with chords transposed by [semitones].
     */
    fun transposeRawChordPro(rawText: String, semitones: Int): String {
        if (semitones == 0) return rawText

        return rawText.lines().joinToString("\n") { line ->
            // Check for {key: G} directive
            val keyMatch = Regex("^(\\s*\\{\\s*(?:key|k)\\s*:\\s*)(.*?)\\s*\\}\\s*$", RegexOption.IGNORE_CASE).find(line)
            if (keyMatch != null) {
                val prefix = keyMatch.groupValues[1]
                val currentKey = keyMatch.groupValues[2]
                val newKey = Transposer.transposeKey(currentKey, semitones)
                "$prefix$newKey}"
            } else {
                // Replace all [Chord] occurrences
                CHORD_REGEX.replace(line) { matchResult ->
                    val chord = matchResult.groupValues[1]
                    val transposed = Transposer.transposeChord(chord, semitones)
                    "[$transposed]"
                }
            }
        }
    }
}
