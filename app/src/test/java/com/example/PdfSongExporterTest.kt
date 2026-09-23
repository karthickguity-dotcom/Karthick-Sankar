package com.example

import com.example.data.parser.AccidentalMode
import com.example.data.parser.ChordProParser
import com.example.data.parser.Transposer
import com.example.util.pdf.PdfExportOptions
import com.example.util.pdf.PdfFontSizeMode
import com.example.util.pdf.PdfPageSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PdfSongExporterTest {

    private val sampleChordPro = """
        {title: Amazing Grace}
        {artist: John Newton}
        {key: G}
        {tempo: 72}
        {time: 3/4}
        {capo: 2}

        {comment: Acoustic guitar intro}

        [Verse 1]
        A[G]mazing grace how [C]sweet the [G]sound
        That saved a [D]wretch like me
        I [G]once was lost but [C]now am [G]found
        Was [Em]blind but [D]now I [G]see

        [Chorus]
        Praise [C]God, praise [G]God
        Praise [Em]God, who sets us [D]free
    """.trimIndent()

    @Test
    fun testPdfPageSizes() {
        assertEquals("A4 width in pt", 595, PdfPageSize.A4.width)
        assertEquals("A4 height in pt", 842, PdfPageSize.A4.height)
        assertEquals("Letter width in pt", 612, PdfPageSize.LETTER.width)
        assertEquals("Letter height in pt", 792, PdfPageSize.LETTER.height)
        assertTrue(PdfPageSize.A4.label.contains("210"))
        assertTrue(PdfPageSize.LETTER.label.contains("8.5"))
    }

    @Test
    fun testPdfFontSizes() {
        assertTrue(PdfFontSizeMode.COMPACT.chordSize < PdfFontSizeMode.STANDARD.chordSize)
        assertTrue(PdfFontSizeMode.STANDARD.chordSize < PdfFontSizeMode.LARGE.chordSize)
        assertTrue(PdfFontSizeMode.COMPACT.lyricSize < PdfFontSizeMode.STANDARD.lyricSize)
        assertTrue(PdfFontSizeMode.STANDARD.lyricSize < PdfFontSizeMode.LARGE.lyricSize)
    }

    @Test
    fun testPdfExportOptionsDefaults() {
        val options = PdfExportOptions()
        assertEquals(PdfPageSize.A4, options.pageSize)
        assertEquals(PdfFontSizeMode.STANDARD, options.fontSizeMode)
        assertTrue(options.includeMetadata)
        assertTrue(options.includeFooter)
    }

    @Test
    fun testSongParsingForPdf() {
        val parsed = ChordProParser.parse(sampleChordPro, "Amazing Grace")
        assertEquals("Amazing Grace", parsed.title)
        assertEquals("John Newton", parsed.artist)
        assertEquals("G", parsed.key)
        assertEquals("72", parsed.tempo)
        assertEquals("3/4", parsed.timeSignature)
        assertEquals(2, parsed.capo)
        assertTrue(parsed.sections.isNotEmpty())

        // Test transposition of chords for PDF output
        val transposedGInFlat = Transposer.transposeChord("G", 1, AccidentalMode.FLAT)
        assertEquals("Ab", transposedGInFlat)

        val transposedGInSharp = Transposer.transposeChord("G", 1, AccidentalMode.SHARP)
        assertEquals("G#", transposedGInSharp)

        val transposedKeyFlat = Transposer.transposeKey("G", 3, AccidentalMode.FLAT)
        assertEquals("Bb", transposedKeyFlat)
    }
}
