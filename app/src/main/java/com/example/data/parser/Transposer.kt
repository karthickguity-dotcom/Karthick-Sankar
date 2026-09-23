package com.example.data.parser

enum class AccidentalMode(val symbol: String, val label: String) {
    AUTO("Auto", "Auto"),
    SHARP("♯", "Sharp"),
    FLAT("♭", "Flat");

    companion object {
        fun fromString(value: String?): AccidentalMode {
            return when (value?.uppercase()) {
                "FLAT", "FLATS" -> FLAT
                "AUTO" -> AUTO
                else -> SHARP
            }
        }
    }
}

object Transposer {
    val SHARPS = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val FLATS  = listOf("C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B")

    private val NOTE_TO_INDEX = mapOf(
        "C" to 0, "B#" to 0,
        "C#" to 1, "DB" to 1, "Db" to 1,
        "D" to 2,
        "D#" to 3, "EB" to 3, "Eb" to 3,
        "E" to 4, "FB" to 4,
        "F" to 5, "E#" to 5,
        "F#" to 6, "GB" to 6, "Gb" to 6,
        "G" to 7,
        "G#" to 8, "AB" to 8, "Ab" to 8,
        "A" to 9,
        "A#" to 10, "BB" to 10, "Bb" to 10,
        "B" to 11, "CB" to 11
    )

    fun transposeKey(key: String, semitones: Int, mode: AccidentalMode = AccidentalMode.AUTO): String {
        if (key.isBlank()) return key
        if (semitones == 0 && mode == AccidentalMode.AUTO) return key
        return transposeChord(key, semitones, mode)
    }

    fun transposeChord(chord: String, semitones: Int, mode: AccidentalMode = AccidentalMode.AUTO): String {
        if (chord.isBlank()) return chord
        if (semitones == 0 && mode == AccidentalMode.AUTO) return chord

        // Handle slash chords like G/B, C/E, D/F#
        if (chord.contains("/")) {
            val parts = chord.split("/", limit = 2)
            val mainChord = transposeSingleChord(parts[0], semitones, mode)
            val bassChord = transposeSingleChord(parts[1], semitones, mode)
            return "$mainChord/$bassChord"
        }

        return transposeSingleChord(chord, semitones, mode)
    }

    private fun transposeSingleChord(chord: String, semitones: Int, mode: AccidentalMode): String {
        if (chord.isBlank()) return chord

        // Extract root note: e.g. "C#m7" -> root "C#", suffix "m7"
        val rootLength = if (chord.length > 1 && (chord[1] == '#' || chord[1] == 'b' || chord[1] == 'B')) 2 else 1
        val root = chord.substring(0, rootLength)
        val suffix = chord.substring(rootLength)

        val upperRoot = root.replaceFirstChar { it.uppercase() }
        val noteIndex = NOTE_TO_INDEX[upperRoot] ?: return chord

        val newIndex = Math.floorMod(noteIndex + semitones, 12)

        val newRoot = when (mode) {
            AccidentalMode.SHARP -> SHARPS[newIndex]
            AccidentalMode.FLAT -> FLATS[newIndex]
            AccidentalMode.AUTO -> {
                val preferFlats = when {
                    root.contains("#") -> false
                    root.contains("b", ignoreCase = true) -> true
                    root.equals("F", ignoreCase = true) -> true
                    semitones < 0 -> true
                    else -> false
                }
                if (preferFlats) FLATS[newIndex] else SHARPS[newIndex]
            }
        }

        return "$newRoot$suffix"
    }
}
