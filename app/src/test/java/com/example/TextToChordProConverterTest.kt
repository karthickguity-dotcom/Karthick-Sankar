package com.example

import com.example.data.parser.TextToChordProConverter
import org.junit.Assert.assertTrue
import org.junit.Test

class TextToChordProConverterTest {

    @Test
    fun testMergeChordsOverLyrics() {
        val input = """
            Title: Amazing Grace
            Key: G
            
            Verse 1:
            G                 C                 G
            Amazing grace how sweet the sound
                 Em        D
            That saved a wretch like me
        """.trimIndent()

        val converted = TextToChordProConverter.convert(input)

        assertTrue(converted.contains("{title: Amazing Grace}"))
        assertTrue(converted.contains("{key: G}"))
        assertTrue(converted.contains("{comment: Verse 1}"))
        assertTrue(converted.contains("[G]Amazing grace") || converted.contains("[G] Amazing grace"))
        assertTrue(converted.contains("[C]"))
        assertTrue(converted.contains("[Em]"))
        assertTrue(converted.contains("[D]"))
    }

    @Test
    fun testChorusBlock() {
        val input = """
            Chorus:
            C             G
            Hallelujah to the King
        """.trimIndent()

        val converted = TextToChordProConverter.convert(input)
        assertTrue(converted.contains("{soc}"))
        assertTrue(converted.contains("[C]"))
        assertTrue(converted.contains("[G]"))
        assertTrue(converted.contains("{eoc}"))
    }

    @Test
    fun testMultilingualTamilChordsOverLyrics() {
        val input = """
            G           C           D
            என் உள்ளமே ஆண்டவரைப் பாடு
        """.trimIndent()

        val converted = TextToChordProConverter.convert(input)
        assertTrue(converted.contains("[G]"))
        assertTrue(converted.contains("[C]"))
        assertTrue(converted.contains("[D]"))
        assertTrue(converted.contains("என் உள்ளமே"))
    }

    @Test
    fun testTitleExtractionForSaveDialog() {
        val input = "Title: 10,000 Reasons\nKey: D\n\nChorus:\n[D]Bless the Lord"
        val converted = TextToChordProConverter.convert(input)
        val detected = Regex("""\{title:\s*([^}]+)\}""", RegexOption.IGNORE_CASE)
            .find(converted)?.groupValues?.get(1)?.trim()
        org.junit.Assert.assertEquals("10,000 Reasons", detected)
    }

    @Test
    fun testEnsureChordProTitleWithUserCustomName() {
        val originalChordPro = "{title: Old Name}\n[G]Amazing Grace"
        val userChosenName = "Sunday Acoustic Special"

        val titleRegex = Regex("""^(\s*\{\s*(?:title|t)\s*:\s*)(.*?)\s*\}\s*$""", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        val updatedChordPro = if (titleRegex.containsMatchIn(originalChordPro)) {
            titleRegex.replace(originalChordPro) { matchResult ->
                "${matchResult.groupValues[1]}$userChosenName}"
            }
        } else {
            "{title: $userChosenName}\n$originalChordPro"
        }

        assertTrue(updatedChordPro.contains("{title: Sunday Acoustic Special}"))
        org.junit.Assert.assertFalse(updatedChordPro.contains("{title: Old Name}"))
    }
}
