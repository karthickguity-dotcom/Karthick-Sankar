package com.example.util.chord

enum class InstrumentType(val displayName: String, val iconEmoji: String) {
    PIANO("Piano", "🎹"),
    GUITAR("Guitar", "🎸"),
    BOTH("Both", "🎹+🎸"),
    NONE("Hide", "✕");

    companion object {
        fun fromString(value: String?): InstrumentType {
            return when (value?.uppercase()) {
                "PIANO" -> PIANO
                "GUITAR" -> GUITAR
                "BOTH" -> BOTH
                else -> NONE
            }
        }
    }
}

/**
 * Standard Guitar chord voicing for a 6-string guitar (tuning E-A-D-G-B-E).
 * @param chordName Name of the chord (e.g. "C", "Am", "G7")
 * @param baseFret The first fret displayed on the diagram (1 = nut, >1 = fret offset)
 * @param frets Fret positions for strings 6 to 1 (low E to high E).
 *              -1 = muted (X), 0 = open (O), 1..N = fret offset relative to baseFret
 * @param fingers Optional finger numbers (1=index, 2=middle, 3=ring, 4=pinky) for strings 6 to 1
 * @param barres Fret numbers relative to baseFret that have a barre across multiple strings
 */
data class GuitarChordVoicing(
    val chordName: String,
    val baseFret: Int = 1,
    val frets: List<Int>, // 6 elements: string 6 down to string 1
    val fingers: List<Int> = emptyList(),
    val barres: List<Int> = emptyList()
)

/**
 * Piano chord voicing representing keys to press on a keyboard.
 * @param chordName Name of the chord
 * @param rootNote The root note (e.g. "C", "F#")
 * @param notes The note names in the chord (e.g. ["C", "E", "G"])
 * @param semitoneOffsets Semitone offsets from C3/C4 (0..23 across 2 octaves)
 */
data class PianoChordVoicing(
    val chordName: String,
    val rootNote: String,
    val notes: List<String>,
    val semitoneOffsets: List<Int>
)
