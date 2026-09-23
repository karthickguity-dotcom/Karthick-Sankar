package com.example.data.parser

object TextToChordProConverter {

    // Regex to match a chord token (e.g. C, G, Am, F#m7, Bbmaj7, D/F#, Caug, Bsus4, G7, etc.)
    private val CHORD_REGEX = Regex(
        "^(?:\\[)?([A-G][b#]?(?:m|min|maj|dim|aug|sus|add|M)?[0-9]*(?:(?:b|#)[0-9]+)*(?:/[A-G][b#]?)?)(?:\\])?$",
        RegexOption.IGNORE_CASE
    )

    private val DIRECTIVE_KEYWORD_REGEX = Regex(
        "^(Title|Artist|Key|Capo|Tempo)\\s*:\\s*(.*)$",
        RegexOption.IGNORE_CASE
    )

    private val SECTION_HEADER_REGEX = Regex(
        "^(?:\\[)?(Verse\\s*\\d*|Chorus|Bridge|Intro|Outro|Pre-Chorus|Ending|Hook|Refrain)(?:\\])?\\s*:?$",
        RegexOption.IGNORE_CASE
    )

    /**
     * Converts raw text (with chords on separate lines or chords above lyrics)
     * into clean, standardized ChordPro format.
     */
    fun convert(rawText: String): String {
        if (rawText.isBlank()) return ""

        val rawLines = rawText.lines()
        val outputLines = mutableListOf<String>()

        var i = 0
        var insideChorus = false

        while (i < rawLines.size) {
            val line = rawLines[i]
            val trimmed = line.trim()

            // 1. Blank line
            if (trimmed.isEmpty()) {
                if (insideChorus) {
                    outputLines.add("{eoc}")
                    insideChorus = false
                }
                outputLines.add("")
                i++
                continue
            }

            // 2. Directive line, e.g. "Title: Amazing Grace", "Key: G"
            val directiveMatch = DIRECTIVE_KEYWORD_REGEX.find(trimmed)
            if (directiveMatch != null) {
                val key = directiveMatch.groupValues[1].lowercase()
                val value = directiveMatch.groupValues[2].trim()
                outputLines.add("{$key: $value}")
                i++
                continue
            }

            // 3. Section header, e.g. "Chorus:", "[Chorus]", "Verse 1:"
            val sectionMatch = SECTION_HEADER_REGEX.find(trimmed)
            if (sectionMatch != null) {
                if (insideChorus) {
                    outputLines.add("{eoc}")
                    insideChorus = false
                }

                val header = sectionMatch.groupValues[1].trim()
                if (header.startsWith("chorus", ignoreCase = true)) {
                    outputLines.add("{soc}")
                    insideChorus = true
                } else {
                    outputLines.add("{comment: $header}")
                }
                i++
                continue
            }

            // 4. Already ChordPro bracketed directive or tag
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                outputLines.add(trimmed)
                i++
                continue
            }

            // 5. Check if current line is a chords line
            if (isChordLine(line)) {
                val chordsWithPositions = extractChordsWithPositions(line)

                // Check if next line is a lyric line (not empty, not chord line, not directive)
                val nextLine = rawLines.getOrNull(i + 1)
                if (nextLine != null && nextLine.isNotBlank() && !isChordLine(nextLine) && !isDirectiveOrHeader(nextLine)) {
                    // Merge chords into next line lyrics
                    val mergedLine = mergeChordsIntoLyrics(chordsWithPositions, nextLine)
                    outputLines.add(mergedLine)
                    i += 2 // skip both chord line and lyric line
                    continue
                } else {
                    // Standalone chord line (e.g. Intro / Outro chords)
                    val standaloneLine = formatStandaloneChords(chordsWithPositions)
                    outputLines.add(standaloneLine)
                    i++
                    continue
                }
            }

            // 6. Normal lyrics line
            outputLines.add(line)
            i++
        }

        if (insideChorus) {
            outputLines.add("{eoc}")
        }

        return outputLines.joinToString("\n")
    }

    private fun isDirectiveOrHeader(line: String): Boolean {
        val trimmed = line.trim()
        return DIRECTIVE_KEYWORD_REGEX.matches(trimmed) ||
                SECTION_HEADER_REGEX.matches(trimmed) ||
                (trimmed.startsWith("{") && trimmed.endsWith("}"))
    }

    /**
     * Determines whether a line contains primarily chords.
     */
    fun isChordLine(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return false

        // Split by whitespace
        val tokens = trimmed.split(Regex("\\s+")).filter { it.isNotEmpty() }
        if (tokens.isEmpty()) return false

        var chordCount = 0
        for (token in tokens) {
            // Strip punctuation like parentheses, commas, colons
            val cleanToken = token.trim('(', ')', '[', ']', ',', ':', '|')
            if (isSingleChord(cleanToken)) {
                chordCount++
            }
        }

        // A line is a chord line if at least 65% of tokens are valid chords
        val ratio = chordCount.toDouble() / tokens.size
        return ratio >= 0.65
    }

    private fun isSingleChord(token: String): Boolean {
        if (token.isEmpty()) return false
        // Exclude common short lyric words that might match uppercase letters
        val forbiddenLyrics = setOf("A", "I", "IN", "AM", "AT", "ON", "TO", "SO", "GO", "NO", "ME", "HE", "WE")
        if (token in forbiddenLyrics) {
            // "A" can be chord A. In a chord line, A usually appears alongside other chords or with brackets
            return token == "A" || token == "Am"
        }
        return CHORD_REGEX.matches(token)
    }

    data class ChordPosition(val chord: String, val columnIndex: Int)

    private fun extractChordsWithPositions(line: String): List<ChordPosition> {
        val result = mutableListOf<ChordPosition>()
        val regex = Regex("(?:\\[)?([A-G][b#]?(?:m|min|maj|dim|aug|sus|add|M)?[0-9]*(?:(?:b|#)[0-9]+)*(?:/[A-G][b#]?)?)(?:\\])?")
        val matches = regex.findAll(line)
        for (match in matches) {
            val chordName = match.groupValues[1]
            result.add(ChordPosition(chordName, match.range.first))
        }
        return result
    }

    private fun mergeChordsIntoLyrics(chords: List<ChordPosition>, lyrics: String): String {
        if (chords.isEmpty()) return lyrics

        val sb = StringBuilder()
        var currentLyricCol = 0

        for (chordPos in chords) {
            val targetCol = chordPos.columnIndex

            // Append lyrics up to this chord's column
            if (targetCol > currentLyricCol) {
                if (currentLyricCol < lyrics.length) {
                    val endCol = targetCol.coerceAtMost(lyrics.length)
                    sb.append(lyrics.substring(currentLyricCol, endCol))
                    currentLyricCol = endCol
                }

                // If target column extends past the end of the lyrics, pad with space
                if (targetCol > lyrics.length) {
                    val spacesToPad = (targetCol - lyrics.length).coerceAtMost(4)
                    sb.append(" ".repeat(spacesToPad))
                }
            }

            // Insert bracketed chord
            sb.append("[${chordPos.chord}]")
        }

        // Append remaining lyrics
        if (currentLyricCol < lyrics.length) {
            sb.append(lyrics.substring(currentLyricCol))
        }

        return sb.toString()
    }

    private fun formatStandaloneChords(chords: List<ChordPosition>): String {
        return chords.joinToString(" ") { "[${it.chord}]" }
    }
}
