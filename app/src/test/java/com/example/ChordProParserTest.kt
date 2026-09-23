package com.example

import com.example.data.parser.AccidentalMode
import com.example.data.parser.ChordProParser
import com.example.data.parser.Transposer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordProParserTest {

    @Test
    fun testSharpAndFlatAccidentalModes() {
        // Sharp Mode: forced sharp enharmonics
        assertEquals("C#", Transposer.transposeChord("Db", 0, AccidentalMode.SHARP))
        assertEquals("D#", Transposer.transposeChord("Eb", 0, AccidentalMode.SHARP))
        assertEquals("F#", Transposer.transposeChord("Gb", 0, AccidentalMode.SHARP))
        assertEquals("G#", Transposer.transposeChord("Ab", 0, AccidentalMode.SHARP))
        assertEquals("A#", Transposer.transposeChord("Bb", 0, AccidentalMode.SHARP))
        assertEquals("A#m7", Transposer.transposeChord("Bbm7", 0, AccidentalMode.SHARP))
        assertEquals("C#/F", Transposer.transposeChord("Db/F", 0, AccidentalMode.SHARP))
        assertEquals("C#", Transposer.transposeKey("Db", 0, AccidentalMode.SHARP))

        // Flat Mode: forced flat enharmonics
        assertEquals("Db", Transposer.transposeChord("C#", 0, AccidentalMode.FLAT))
        assertEquals("Eb", Transposer.transposeChord("D#", 0, AccidentalMode.FLAT))
        assertEquals("Gb", Transposer.transposeChord("F#", 0, AccidentalMode.FLAT))
        assertEquals("Ab", Transposer.transposeChord("G#", 0, AccidentalMode.FLAT))
        assertEquals("Bb", Transposer.transposeChord("A#", 0, AccidentalMode.FLAT))
        assertEquals("Bbm7", Transposer.transposeChord("A#m7", 0, AccidentalMode.FLAT))
        assertEquals("D/Gb", Transposer.transposeChord("D/F#", 0, AccidentalMode.FLAT))
        assertEquals("Db", Transposer.transposeKey("C#", 0, AccidentalMode.FLAT))

        // Transposition with Sharp Mode
        assertEquals("C#", Transposer.transposeChord("C", 1, AccidentalMode.SHARP))
        assertEquals("D#", Transposer.transposeChord("C", 3, AccidentalMode.SHARP))
        assertEquals("A#", Transposer.transposeChord("C", -2, AccidentalMode.SHARP))

        // Transposition with Flat Mode
        assertEquals("Db", Transposer.transposeChord("C", 1, AccidentalMode.FLAT))
        assertEquals("Eb", Transposer.transposeChord("C", 3, AccidentalMode.FLAT))
        assertEquals("Bb", Transposer.transposeChord("C", -2, AccidentalMode.FLAT))

        // Transposition of Keys with both Sharp and Flat Modes respecting offset
        assertEquals("C#", Transposer.transposeKey("C", 1, AccidentalMode.SHARP))
        assertEquals("Db", Transposer.transposeKey("C", 1, AccidentalMode.FLAT))
        assertEquals("D#", Transposer.transposeKey("C", 3, AccidentalMode.SHARP))
        assertEquals("Eb", Transposer.transposeKey("C", 3, AccidentalMode.FLAT))
        assertEquals("G#", Transposer.transposeKey("G", 1, AccidentalMode.SHARP))
        assertEquals("Ab", Transposer.transposeKey("G", 1, AccidentalMode.FLAT))
        assertEquals("A#", Transposer.transposeKey("C", -2, AccidentalMode.SHARP))
        assertEquals("Bb", Transposer.transposeKey("C", -2, AccidentalMode.FLAT))
        // Minor keys
        assertEquals("G#m", Transposer.transposeKey("F#m", 2, AccidentalMode.SHARP))
        assertEquals("Abm", Transposer.transposeKey("F#m", 2, AccidentalMode.FLAT))
    }

    @Test
    fun testTransposeBasicChords() {
        assertEquals("D", Transposer.transposeChord("C", 2))
        assertEquals("G#", Transposer.transposeChord("G", 1))
        assertEquals("C", Transposer.transposeChord("C", 0))
        assertEquals("C", Transposer.transposeChord("C", 12))
        assertEquals("Bb", Transposer.transposeChord("C", -2))
        assertEquals("F#m", Transposer.transposeChord("Em", 2))
        assertEquals("C7", Transposer.transposeChord("B7", 1))
        assertEquals("Am7", Transposer.transposeChord("Gm7", 2))
    }

    @Test
    fun testTransposeSlashChords() {
        // D/F# transposed up 2 semitones becomes E/G#
        assertEquals("E/G#", Transposer.transposeChord("D/F#", 2))
        // C/E transposed down 2 semitones becomes Bb/D
        assertEquals("Bb/D", Transposer.transposeChord("C/E", -2))
    }

    @Test
    fun testParseDirectives() {
        val chordPro = """
            {title: Amazing Grace}
            {artist: John Newton}
            {key: G}
            {capo: 2}
            {tempo: 75}
            {time: 3/4}
            {comment: Verse 1}
            [G]Amazing [C]grace how [G]sweet
        """.trimIndent()

        val parsed = ChordProParser.parse(chordPro)
        assertEquals("Amazing Grace", parsed.title)
        assertEquals("John Newton", parsed.artist)
        assertEquals("G", parsed.key)
        assertEquals(2, parsed.capo)
        assertEquals("75", parsed.tempo)
        assertEquals("3/4", parsed.timeSignature)
        assertTrue(parsed.sections.isNotEmpty())
    }

    @Test
    fun testTempoAndTimeSignatureAliases() {
        val chordProWithAliases = """
            {title: Worship Song}
            {bpm: 128}
            {signature: 6/8}
            [D]Praise the Lord
        """.trimIndent()

        val parsed = ChordProParser.parse(chordProWithAliases)
        assertEquals("128", parsed.tempo)
        assertEquals("6/8", parsed.timeSignature)

        val entity = com.example.data.model.SongEntity(
            title = "Test Song",
            rawChordPro = chordProWithAliases
        )
        assertEquals("128", entity.displayTempo)
        assertEquals("6/8", entity.displayTimeSignature)
    }

    @Test
    fun testParseTamilAndHindiUnicodeLyrics() {
        val tamilChordPro = """
            {title: என் உள்ளமே}
            {key: G}
            [G]என் உள்ளமே [C]ஆண்டவரைப் [D]பாடு
        """.trimIndent()

        val parsed = ChordProParser.parse(tamilChordPro)
        assertEquals("என் உள்ளமே", parsed.title)
        assertEquals("G", parsed.key)

        val pairs = parsed.sections.flatMap { it.lines }.flatMap { it.pairs }
        // Verify chords and unicode lyrics are accurately captured
        assertTrue(pairs.any { it.chord == "G" && it.lyric.contains("என் உள்ளமே") })
        assertTrue(pairs.any { it.chord == "C" && it.lyric.contains("ஆண்டவரைப்") })
    }

    @Test
    fun testPlainLyricsPreservation() {
        val plainText = """
            Amazing grace how sweet the sound
            That saved a wretch like me
        """.trimIndent()

        val parsed = ChordProParser.parse(plainText, defaultTitle = "Simple Lyrics")
        assertEquals("Simple Lyrics", parsed.title)
        val textLines = parsed.sections.flatMap { it.lines }
        assertTrue(textLines.isNotEmpty())
    }
}
