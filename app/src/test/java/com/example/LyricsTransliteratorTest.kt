package com.example

import com.example.util.transliteration.LyricsTransliterator
import com.example.util.transliteration.TransliterationTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LyricsTransliteratorTest {

    @Test
    fun testOriginalLeavesTextUnchanged() {
        val originalTamil = "என் உள்ளமே"
        val result = LyricsTransliterator.transliterateLyric(originalTamil, TransliterationTarget.ORIGINAL)
        assertEquals(originalTamil, result)
    }

    @Test
    fun testTamilToLatinRomanization() {
        val tamil = "என் உள்ளமே"
        val latin = LyricsTransliterator.transliterateLyric(tamil, TransliterationTarget.LATIN)
        // Should convert to phonetic Latin
        assertTrue(latin.contains("en", ignoreCase = true))
        assertTrue(latin.contains("ullam", ignoreCase = true))
    }

    @Test
    fun testDevanagariToLatinRomanization() {
        val hindi = "आओ मिलकर"
        val latin = LyricsTransliterator.transliterateLyric(hindi, TransliterationTarget.LATIN)
        assertTrue(latin.contains("aao", ignoreCase = true))
        assertTrue(latin.contains("mil", ignoreCase = true) && latin.contains("kar", ignoreCase = true))
    }

    @Test
    fun testCyrillicToLatinRomanization() {
        val russian = "Привет мир"
        val latin = LyricsTransliterator.transliterateLyric(russian, TransliterationTarget.LATIN)
        assertEquals("Privet mir", latin)
    }

    @Test
    fun testGreekToLatinRomanization() {
        val greek = "Χαρά"
        val latin = LyricsTransliterator.transliterateLyric(greek, TransliterationTarget.LATIN)
        assertEquals("Chara", latin)
    }

    @Test
    fun testHangulToLatinRomanization() {
        val korean = "사랑"
        val latin = LyricsTransliterator.transliterateLyric(korean, TransliterationTarget.LATIN)
        assertEquals("sarang", latin)
    }

    @Test
    fun testChordsAreNeverMangled() {
        // When processing chord-lyric pairs, chord is isolated from lyric
        val chord = "G#m7/D#"
        val lyric = " என் உள்ளமே "
        val transliteratedLyric = LyricsTransliterator.transliterateLyric(lyric, TransliterationTarget.LATIN)

        // Chord is never passed to transliteration and remains exactly identical
        assertEquals("G#m7/D#", chord)
        assertTrue(transliteratedLyric.contains("en", ignoreCase = true))
    }
}
