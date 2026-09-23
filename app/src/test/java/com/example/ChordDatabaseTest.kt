package com.example

import com.example.data.parser.AccidentalMode
import com.example.util.chord.ChordDatabase
import com.example.util.chord.InstrumentType
import com.example.util.pdf.PdfExportOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChordDatabaseTest {

    @Test
    fun testGuitarChordLookup_OpenChords() {
        val cChord = ChordDatabase.getGuitarChord("C")
        assertEquals("C", cChord.chordName)
        assertEquals(1, cChord.baseFret)
        assertEquals(listOf(-1, 3, 2, 0, 1, 0), cChord.frets)

        val gChord = ChordDatabase.getGuitarChord("G")
        assertEquals("G", gChord.chordName)
        assertEquals(listOf(3, 2, 0, 0, 0, 3), gChord.frets)

        val dChord = ChordDatabase.getGuitarChord("D")
        assertEquals(listOf(-1, -1, 0, 2, 3, 2), dChord.frets)

        val emChord = ChordDatabase.getGuitarChord("Em")
        assertEquals(listOf(0, 2, 2, 0, 0, 0), emChord.frets)
    }

    @Test
    fun testGuitarChordLookup_BarreAndSlashChords() {
        // F is a barre chord at fret 1
        val fChord = ChordDatabase.getGuitarChord("F")
        assertEquals(1, fChord.baseFret)
        assertTrue("F barre at fret 1", fChord.barres.contains(1))

        // Bm is a barre chord at fret 2
        val bmChord = ChordDatabase.getGuitarChord("Bm")
        assertEquals(2, bmChord.baseFret)
        assertTrue("Bm barre at fret 1 (offset from fret 2)", bmChord.barres.contains(1))

        // Slash chord G/B should resolve base voicing for G
        val gSlashB = ChordDatabase.getGuitarChord("G/B")
        assertNotNull(gSlashB)
        assertEquals("G/B", gSlashB.chordName)
        assertEquals(6, gSlashB.frets.size)
    }

    @Test
    fun testPianoChord_NotesAndSemitoneCalculation() {
        // C Major: C (0), E (4), G (7)
        val cPiano = ChordDatabase.getPianoChord("C")
        assertEquals("C", cPiano.rootNote)
        assertTrue(cPiano.notes.contains("C"))
        assertTrue(cPiano.notes.contains("E"))
        assertTrue(cPiano.notes.contains("G"))
        assertTrue(cPiano.semitoneOffsets.contains(0))
        assertTrue(cPiano.semitoneOffsets.contains(4))
        assertTrue(cPiano.semitoneOffsets.contains(7))

        // A Minor: A, C, E
        val amPiano = ChordDatabase.getPianoChord("Am")
        assertEquals("A", amPiano.rootNote)
        assertTrue(amPiano.notes.contains("A"))
        assertTrue(amPiano.notes.contains("C"))
        assertTrue(amPiano.notes.contains("E"))

        // G7: G, B, D, F
        val g7Piano = ChordDatabase.getPianoChord("G7")
        assertTrue(g7Piano.notes.contains("G"))
        assertTrue(g7Piano.notes.contains("B"))
        assertTrue(g7Piano.notes.contains("D"))
        assertTrue(g7Piano.notes.contains("F"))
    }

    @Test
    fun testPianoChord_AccidentalModes() {
        val dbPianoSharp = ChordDatabase.getPianoChord("Db", AccidentalMode.SHARP)
        val dbPianoFlat = ChordDatabase.getPianoChord("Db", AccidentalMode.FLAT)

        assertNotNull(dbPianoSharp)
        assertNotNull(dbPianoFlat)
        assertTrue(dbPianoFlat.notes.contains("Db"))
    }

    @Test
    fun testInstrumentType() {
        assertEquals(InstrumentType.GUITAR, InstrumentType.fromString("GUITAR"))
        assertEquals(InstrumentType.PIANO, InstrumentType.fromString("PIANO"))
        assertEquals(InstrumentType.NONE, InstrumentType.fromString("NONE"))
        assertEquals(InstrumentType.NONE, InstrumentType.fromString("other"))
    }

    @Test
    fun testPdfExportOptions_WithInstrument() {
        val optionsGuitar = PdfExportOptions(chordDiagramInstrument = InstrumentType.GUITAR)
        assertEquals(InstrumentType.GUITAR, optionsGuitar.chordDiagramInstrument)

        val optionsPiano = PdfExportOptions(chordDiagramInstrument = InstrumentType.PIANO)
        assertEquals(InstrumentType.PIANO, optionsPiano.chordDiagramInstrument)

        val optionsNone = PdfExportOptions(chordDiagramInstrument = InstrumentType.NONE)
        assertEquals(InstrumentType.NONE, optionsNone.chordDiagramInstrument)
    }
}
