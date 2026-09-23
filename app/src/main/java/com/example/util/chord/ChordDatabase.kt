package com.example.util.chord

import com.example.data.parser.AccidentalMode
import com.example.data.parser.Transposer

object ChordDatabase {

    private val NOTE_TO_SEMITONE = mapOf(
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

    // Common guitar chord shapes: strings 6 to 1 (Low E, A, D, G, B, High E)
    // -1: Mute (X), 0: Open (O), 1..N: fret relative to baseFret
    private val GUITAR_SHAPES = mapOf(
        // C Chords
        "C" to GuitarChordVoicing("C", 1, listOf(-1, 3, 2, 0, 1, 0), listOf(0, 3, 2, 0, 1, 0)),
        "Cm" to GuitarChordVoicing("Cm", 3, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "C7" to GuitarChordVoicing("C7", 1, listOf(-1, 3, 2, 3, 1, 0), listOf(0, 3, 2, 4, 1, 0)),
        "Cmaj7" to GuitarChordVoicing("Cmaj7", 1, listOf(-1, 3, 2, 0, 0, 0), listOf(0, 3, 2, 0, 0, 0)),
        "Cm7" to GuitarChordVoicing("Cm7", 3, listOf(-1, 1, 3, 1, 2, 1), listOf(0, 1, 3, 1, 2, 1), listOf(1)),
        "Csus4" to GuitarChordVoicing("Csus4", 1, listOf(-1, 3, 3, 0, 1, 1), listOf(0, 3, 4, 0, 1, 2)),
        "Csus2" to GuitarChordVoicing("Csus2", 3, listOf(-1, 1, 3, 3, 1, 1), listOf(0, 1, 3, 4, 1, 1), listOf(1)),
        "Cadd9" to GuitarChordVoicing("Cadd9", 1, listOf(-1, 3, 2, 0, 3, 0), listOf(0, 2, 1, 0, 3, 0)),
        "C5" to GuitarChordVoicing("C5", 3, listOf(-1, 1, 3, 3, -1, -1), listOf(0, 1, 3, 4, 0, 0)),

        // C# / Db Chords
        "C#" to GuitarChordVoicing("C#", 4, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "Db" to GuitarChordVoicing("Db", 4, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "C#m" to GuitarChordVoicing("C#m", 4, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "Dbm" to GuitarChordVoicing("Dbm", 4, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "C#7" to GuitarChordVoicing("C#7", 4, listOf(-1, 1, 3, 1, 3, 1), listOf(0, 1, 3, 1, 4, 1), listOf(1)),
        "Db7" to GuitarChordVoicing("Db7", 4, listOf(-1, 1, 3, 1, 3, 1), listOf(0, 1, 3, 1, 4, 1), listOf(1)),

        // D Chords
        "D" to GuitarChordVoicing("D", 1, listOf(-1, -1, 0, 2, 3, 2), listOf(0, 0, 0, 1, 3, 2)),
        "Dm" to GuitarChordVoicing("Dm", 1, listOf(-1, -1, 0, 2, 3, 1), listOf(0, 0, 0, 2, 3, 1)),
        "D7" to GuitarChordVoicing("D7", 1, listOf(-1, -1, 0, 2, 1, 2), listOf(0, 0, 0, 2, 1, 3)),
        "Dmaj7" to GuitarChordVoicing("Dmaj7", 1, listOf(-1, -1, 0, 2, 2, 2), listOf(0, 0, 0, 1, 2, 3)),
        "Dm7" to GuitarChordVoicing("Dm7", 1, listOf(-1, -1, 0, 2, 1, 1), listOf(0, 0, 0, 2, 1, 1), listOf(1)),
        "Dsus4" to GuitarChordVoicing("Dsus4", 1, listOf(-1, -1, 0, 2, 3, 3), listOf(0, 0, 0, 1, 2, 3)),
        "Dsus2" to GuitarChordVoicing("Dsus2", 1, listOf(-1, -1, 0, 2, 3, 0), listOf(0, 0, 0, 1, 2, 0)),
        "Dadd9" to GuitarChordVoicing("Dadd9", 1, listOf(-1, -1, 0, 2, 5, 2), listOf(0, 0, 0, 1, 4, 2)),
        "D5" to GuitarChordVoicing("D5", 1, listOf(-1, -1, 0, 2, 3, -1), listOf(0, 0, 0, 1, 2, 0)),

        // D# / Eb Chords
        "D#" to GuitarChordVoicing("D#", 6, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "Eb" to GuitarChordVoicing("Eb", 6, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "D#m" to GuitarChordVoicing("D#m", 6, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "Ebm" to GuitarChordVoicing("Ebm", 6, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "Eb7" to GuitarChordVoicing("Eb7", 6, listOf(-1, 1, 3, 1, 3, 1), listOf(0, 1, 3, 1, 4, 1), listOf(1)),

        // E Chords
        "E" to GuitarChordVoicing("E", 1, listOf(0, 2, 2, 1, 0, 0), listOf(0, 2, 3, 1, 0, 0)),
        "Em" to GuitarChordVoicing("Em", 1, listOf(0, 2, 2, 0, 0, 0), listOf(0, 2, 3, 0, 0, 0)),
        "E7" to GuitarChordVoicing("E7", 1, listOf(0, 2, 0, 1, 0, 0), listOf(0, 2, 0, 1, 0, 0)),
        "Emaj7" to GuitarChordVoicing("Emaj7", 1, listOf(0, 2, 1, 1, 0, 0), listOf(0, 3, 1, 2, 0, 0)),
        "Em7" to GuitarChordVoicing("Em7", 1, listOf(0, 2, 0, 0, 0, 0), listOf(0, 1, 0, 0, 0, 0)),
        "Esus4" to GuitarChordVoicing("Esus4", 1, listOf(0, 2, 2, 2, 0, 0), listOf(0, 2, 3, 4, 0, 0)),
        "Esus2" to GuitarChordVoicing("Esus2", 1, listOf(0, 2, 4, 4, 0, 0), listOf(0, 1, 3, 4, 0, 0)),
        "E5" to GuitarChordVoicing("E5", 1, listOf(0, 2, 2, -1, -1, -1), listOf(0, 1, 2, 0, 0, 0)),

        // F Chords
        "F" to GuitarChordVoicing("F", 1, listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1), listOf(1)),
        "Fm" to GuitarChordVoicing("Fm", 1, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1)),
        "F7" to GuitarChordVoicing("F7", 1, listOf(1, 3, 1, 2, 1, 1), listOf(1, 3, 1, 2, 1, 1), listOf(1)),
        "Fmaj7" to GuitarChordVoicing("Fmaj7", 1, listOf(-1, -1, 3, 2, 1, 0), listOf(0, 0, 3, 2, 1, 0)),
        "Fm7" to GuitarChordVoicing("Fm7", 1, listOf(1, 3, 1, 1, 1, 1), listOf(1, 3, 1, 1, 1, 1), listOf(1)),
        "Fsus4" to GuitarChordVoicing("Fsus4", 1, listOf(1, 3, 3, 3, 1, 1), listOf(1, 2, 3, 4, 1, 1), listOf(1)),
        "F5" to GuitarChordVoicing("F5", 1, listOf(1, 3, 3, -1, -1, -1), listOf(1, 3, 4, 0, 0, 0)),

        // F# / Gb Chords
        "F#" to GuitarChordVoicing("F#", 2, listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1), listOf(1)),
        "Gb" to GuitarChordVoicing("Gb", 2, listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1), listOf(1)),
        "F#m" to GuitarChordVoicing("F#m", 2, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1)),
        "Gbm" to GuitarChordVoicing("Gbm", 2, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1)),
        "F#7" to GuitarChordVoicing("F#7", 2, listOf(1, 3, 1, 2, 1, 1), listOf(1, 3, 1, 2, 1, 1), listOf(1)),
        "F#m7" to GuitarChordVoicing("F#m7", 2, listOf(1, 3, 1, 1, 1, 1), listOf(1, 3, 1, 1, 1, 1), listOf(1)),

        // G Chords
        "G" to GuitarChordVoicing("G", 1, listOf(3, 2, 0, 0, 0, 3), listOf(2, 1, 0, 0, 0, 3)),
        "Gm" to GuitarChordVoicing("Gm", 3, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1)),
        "G7" to GuitarChordVoicing("G7", 1, listOf(3, 2, 0, 0, 0, 1), listOf(3, 2, 0, 0, 0, 1)),
        "Gmaj7" to GuitarChordVoicing("Gmaj7", 1, listOf(3, 2, 0, 0, 0, 2), listOf(3, 2, 0, 0, 0, 1)),
        "Gm7" to GuitarChordVoicing("Gm7", 3, listOf(1, 3, 1, 1, 1, 1), listOf(1, 3, 1, 1, 1, 1), listOf(1)),
        "Gsus4" to GuitarChordVoicing("Gsus4", 1, listOf(3, 3, 0, 0, 1, 3), listOf(2, 3, 0, 0, 1, 4)),
        "Gsus2" to GuitarChordVoicing("Gsus2", 1, listOf(3, 0, 0, 0, 3, 3), listOf(1, 0, 0, 0, 3, 4)),
        "Gadd9" to GuitarChordVoicing("Gadd9", 1, listOf(3, 2, 0, 2, 0, 3), listOf(2, 1, 0, 3, 0, 4)),
        "G5" to GuitarChordVoicing("G5", 3, listOf(1, 3, 3, -1, -1, -1), listOf(1, 3, 4, 0, 0, 0)),

        // G# / Ab Chords
        "G#" to GuitarChordVoicing("G#", 4, listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1), listOf(1)),
        "Ab" to GuitarChordVoicing("Ab", 4, listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1), listOf(1)),
        "G#m" to GuitarChordVoicing("G#m", 4, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1)),
        "Abm" to GuitarChordVoicing("Abm", 4, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1)),
        "G#7" to GuitarChordVoicing("G#7", 4, listOf(1, 3, 1, 2, 1, 1), listOf(1, 3, 1, 2, 1, 1), listOf(1)),
        "Ab7" to GuitarChordVoicing("Ab7", 4, listOf(1, 3, 1, 2, 1, 1), listOf(1, 3, 1, 2, 1, 1), listOf(1)),

        // A Chords
        "A" to GuitarChordVoicing("A", 1, listOf(-1, 0, 2, 2, 2, 0), listOf(0, 0, 1, 2, 3, 0)),
        "Am" to GuitarChordVoicing("Am", 1, listOf(-1, 0, 2, 2, 1, 0), listOf(0, 0, 2, 3, 1, 0)),
        "A7" to GuitarChordVoicing("A7", 1, listOf(-1, 0, 2, 0, 2, 0), listOf(0, 0, 1, 0, 2, 0)),
        "Amaj7" to GuitarChordVoicing("Amaj7", 1, listOf(-1, 0, 2, 1, 2, 0), listOf(0, 0, 2, 1, 3, 0)),
        "Am7" to GuitarChordVoicing("Am7", 1, listOf(-1, 0, 2, 0, 1, 0), listOf(0, 0, 2, 0, 1, 0)),
        "Asus4" to GuitarChordVoicing("Asus4", 1, listOf(-1, 0, 2, 2, 3, 0), listOf(0, 0, 1, 2, 3, 0)),
        "Asus2" to GuitarChordVoicing("Asus2", 1, listOf(-1, 0, 2, 2, 0, 0), listOf(0, 0, 1, 2, 0, 0)),
        "Aadd9" to GuitarChordVoicing("Aadd9", 1, listOf(-1, 0, 2, 4, 2, 0), listOf(0, 0, 1, 4, 2, 0)),
        "A5" to GuitarChordVoicing("A5", 1, listOf(-1, 0, 2, 2, -1, -1), listOf(0, 0, 1, 2, 0, 0)),

        // A# / Bb Chords
        "A#" to GuitarChordVoicing("A#", 1, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "Bb" to GuitarChordVoicing("Bb", 1, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "A#m" to GuitarChordVoicing("A#m", 1, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "Bbm" to GuitarChordVoicing("Bbm", 1, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "A#7" to GuitarChordVoicing("A#7", 1, listOf(-1, 1, 3, 1, 3, 1), listOf(0, 1, 3, 1, 4, 1), listOf(1)),
        "Bb7" to GuitarChordVoicing("Bb7", 1, listOf(-1, 1, 3, 1, 3, 1), listOf(0, 1, 3, 1, 4, 1), listOf(1)),
        "Bbm7" to GuitarChordVoicing("Bbm7", 1, listOf(-1, 1, 3, 1, 2, 1), listOf(0, 1, 3, 1, 2, 1), listOf(1)),

        // B Chords
        "B" to GuitarChordVoicing("B", 2, listOf(-1, 1, 3, 3, 3, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "Bm" to GuitarChordVoicing("Bm", 2, listOf(-1, 1, 3, 3, 2, 1), listOf(0, 1, 3, 4, 2, 1), listOf(1)),
        "B7" to GuitarChordVoicing("B7", 1, listOf(-1, 2, 1, 2, 0, 2), listOf(0, 2, 1, 3, 0, 4)),
        "Bmaj7" to GuitarChordVoicing("Bmaj7", 2, listOf(-1, 1, 3, 2, 3, 1), listOf(0, 1, 3, 2, 4, 1), listOf(1)),
        "Bm7" to GuitarChordVoicing("Bm7", 2, listOf(-1, 1, 3, 1, 2, 1), listOf(0, 1, 3, 1, 2, 1), listOf(1)),
        "Bsus4" to GuitarChordVoicing("Bsus4", 2, listOf(-1, 1, 3, 3, 4, 1), listOf(0, 1, 2, 3, 4, 1), listOf(1)),
        "Bsus2" to GuitarChordVoicing("Bsus2", 2, listOf(-1, 1, 3, 3, 1, 1), listOf(0, 1, 3, 4, 1, 1), listOf(1)),
        "B5" to GuitarChordVoicing("B5", 2, listOf(-1, 1, 3, 3, -1, -1), listOf(0, 1, 3, 4, 0, 0))
    )

    /**
     * Retrieves guitar chord voicing for any chord name.
     * If not in static database, generates an accurate barre chord voicing dynamically!
     */
    fun getGuitarChord(chordName: String): GuitarChordVoicing {
        val clean = chordName.trim()
        if (clean.isBlank()) return GuitarChordVoicing("?", 1, listOf(0, 0, 0, 0, 0, 0))

        // Check exact match
        GUITAR_SHAPES[clean]?.let { return it }

        // Handle slash chords like G/B, D/F#
        val baseChord = if (clean.contains("/")) clean.substringBefore("/") else clean
        GUITAR_SHAPES[baseChord]?.let {
            return it.copy(chordName = clean)
        }

        // Try standard enharmonic conversion (e.g. C# -> Db or vice versa)
        val enharmonic = when {
            baseChord.startsWith("C#") -> "Db" + baseChord.substring(2)
            baseChord.startsWith("Db") -> "C#" + baseChord.substring(2)
            baseChord.startsWith("D#") -> "Eb" + baseChord.substring(2)
            baseChord.startsWith("Eb") -> "D#" + baseChord.substring(2)
            baseChord.startsWith("F#") -> "Gb" + baseChord.substring(2)
            baseChord.startsWith("Gb") -> "F#" + baseChord.substring(2)
            baseChord.startsWith("G#") -> "Ab" + baseChord.substring(2)
            baseChord.startsWith("Ab") -> "G#" + baseChord.substring(2)
            baseChord.startsWith("A#") -> "Bb" + baseChord.substring(2)
            baseChord.startsWith("Bb") -> "A#" + baseChord.substring(2)
            else -> null
        }

        if (enharmonic != null) {
            GUITAR_SHAPES[enharmonic]?.let {
                return it.copy(chordName = clean)
            }
        }

        // Dynamic Barre chord fallback based on root note on 6th or 5th string
        return generateDynamicBarre(clean)
    }

    private fun generateDynamicBarre(chord: String): GuitarChordVoicing {
        val rootLength = if (chord.length > 1 && (chord[1] == '#' || chord[1] == 'b' || chord[1] == 'B')) 2 else 1
        val root = chord.substring(0, rootLength).replaceFirstChar { it.uppercase() }
        val suffix = chord.substring(rootLength).lowercase()

        val rootSemitone = NOTE_TO_SEMITONE[root] ?: 0

        // Preferred E-string barre (String 6): E=0, F=1, F#=2, G=3, G#=4, A=5...
        // Fret on String 6 = rootSemitone - 4 (E is 4)
        val fretOnE = Math.floorMod(rootSemitone - 4, 12)
        val baseFret = if (fretOnE == 0) 12 else fretOnE

        val isMinor = suffix.startsWith("m") && !suffix.startsWith("maj")
        val is7 = suffix == "7" || suffix == "dom7"
        val isMinor7 = suffix == "m7" || suffix == "min7"
        val isMaj7 = suffix == "maj7" || suffix == "m7+"

        return when {
            isMinor7 -> GuitarChordVoicing(chord, baseFret, listOf(1, 3, 1, 1, 1, 1), listOf(1, 3, 1, 1, 1, 1), listOf(1))
            is7 -> GuitarChordVoicing(chord, baseFret, listOf(1, 3, 1, 2, 1, 1), listOf(1, 3, 1, 2, 1, 1), listOf(1))
            isMaj7 -> GuitarChordVoicing(chord, baseFret, listOf(1, 3, 2, 2, 1, 1), listOf(1, 3, 2, 2, 1, 1), listOf(1))
            isMinor -> GuitarChordVoicing(chord, baseFret, listOf(1, 3, 3, 1, 1, 1), listOf(1, 3, 4, 1, 1, 1), listOf(1))
            else -> GuitarChordVoicing(chord, baseFret, listOf(1, 3, 3, 2, 1, 1), listOf(1, 3, 4, 2, 1, 1), listOf(1))
        }
    }

    /**
     * Dynamically generates piano keyboard voicing for ANY chord.
     * Computes root note, scale intervals, note names, and keys to illuminate on a piano.
     */
    fun getPianoChord(chordName: String, accidentalMode: AccidentalMode = AccidentalMode.AUTO): PianoChordVoicing {
        val clean = chordName.trim()
        if (clean.isBlank()) return PianoChordVoicing("?", "C", listOf("C"), listOf(0))

        val baseChord = if (clean.contains("/")) clean.substringBefore("/") else clean
        val bassPart = if (clean.contains("/")) clean.substringAfter("/") else null

        val rootLength = if (baseChord.length > 1 && (baseChord[1] == '#' || baseChord[1] == 'b' || baseChord[1] == 'B')) 2 else 1
        val root = baseChord.substring(0, rootLength).replaceFirstChar { it.uppercase() }
        val suffix = baseChord.substring(rootLength).lowercase()

        val rootIndex = NOTE_TO_SEMITONE[root] ?: 0

        // Intervals relative to root note
        val intervals: List<Int> = when {
            // Minor Chords
            suffix == "m" || suffix == "min" || suffix == "-" -> listOf(0, 3, 7)
            suffix == "m7" || suffix == "min7" -> listOf(0, 3, 7, 10)
            suffix == "mmaj7" || suffix == "m(maj7)" -> listOf(0, 3, 7, 11)
            suffix == "m6" || suffix == "min6" -> listOf(0, 3, 7, 9)
            suffix == "m9" || suffix == "min9" -> listOf(0, 3, 7, 10, 14)

            // Major & Extensions
            suffix == "maj7" || suffix == "m7+" || suffix == "delta" || suffix == "maj" -> listOf(0, 4, 7, 11)
            suffix == "7" || suffix == "dom7" -> listOf(0, 4, 7, 10)
            suffix == "9" || suffix == "dom9" -> listOf(0, 4, 7, 10, 14)
            suffix == "add9" || suffix == "2" -> listOf(0, 2, 4, 7)
            suffix == "6" -> listOf(0, 4, 7, 9)
            suffix == "6/9" || suffix == "69" -> listOf(0, 4, 7, 9, 14)

            // Suspended
            suffix == "sus4" || suffix == "sus" -> listOf(0, 5, 7)
            suffix == "sus2" -> listOf(0, 2, 7)
            suffix == "7sus4" || suffix == "7sus" -> listOf(0, 5, 7, 10)

            // Diminished & Augmented
            suffix == "dim" || suffix == "o" || suffix == "°" -> listOf(0, 3, 6)
            suffix == "dim7" || suffix == "o7" || suffix == "°7" -> listOf(0, 3, 6, 9)
            suffix == "m7b5" || suffix == "ø" -> listOf(0, 3, 6, 10)
            suffix == "aug" || suffix == "+" || suffix == "+5" -> listOf(0, 4, 8)
            suffix == "7#5" || suffix == "7+5" -> listOf(0, 4, 8, 10)

            // Power chord
            suffix == "5" -> listOf(0, 7)

            // Standard Major Triad default
            else -> listOf(0, 4, 7)
        }

        // Determine note names and keyboard semitone positions (mapped starting from C3 = 0, C4 = 12)
        // We arrange the chord nicely within a 2-octave span (0..23)
        // Place root note around middle C (index 0 to 11)
        val noteNamesList = mutableListOf<String>()
        val keyboardOffsets = mutableListOf<Int>()

        // Add slash bass note if present (at lower octave)
        if (!bassPart.isNullOrBlank()) {
            val bassRootLength = if (bassPart.length > 1 && (bassPart[1] == '#' || bassPart[1] == 'b')) 2 else 1
            val bassRoot = bassPart.substring(0, bassRootLength).replaceFirstChar { it.uppercase() }
            val bassIndex = NOTE_TO_SEMITONE[bassRoot] ?: 0
            val bassNoteName = if (accidentalMode == AccidentalMode.FLAT || (accidentalMode == AccidentalMode.AUTO && bassRoot.contains("b"))) {
                Transposer.FLATS[bassIndex]
            } else {
                Transposer.SHARPS[bassIndex]
            }
            noteNamesList.add(bassNoteName)
            keyboardOffsets.add(bassIndex) // Place in lower register
        }

        intervals.forEach { interval ->
            val noteSemitone = Math.floorMod(rootIndex + interval, 12)
            val noteName = if (accidentalMode == AccidentalMode.FLAT || (accidentalMode == AccidentalMode.AUTO && (root.contains("b") || root == "F"))) {
                Transposer.FLATS[noteSemitone]
            } else {
                Transposer.SHARPS[noteSemitone]
            }
            if (!noteNamesList.contains(noteName)) {
                noteNamesList.add(noteName)
            }

            // Calculate keyboard display position (0..23)
            val keyPos = if (bassPart.isNullOrBlank()) {
                rootIndex + interval
            } else {
                // If slash chord, shift right hand triad up by 12 if needed
                rootIndex + interval + 12
            }
            // Fit within 0..23
            val normalizedKeyPos = Math.floorMod(keyPos, 24)
            if (!keyboardOffsets.contains(normalizedKeyPos)) {
                keyboardOffsets.add(normalizedKeyPos)
            }
        }

        return PianoChordVoicing(
            chordName = clean,
            rootNote = root,
            notes = noteNamesList,
            semitoneOffsets = keyboardOffsets.sorted()
        )
    }
}
