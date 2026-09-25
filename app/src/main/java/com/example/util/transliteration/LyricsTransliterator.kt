package com.example.util.transliteration

enum class TransliterationTarget(
    val id: String,
    val displayName: String,
    val nativeLabel: String,
    val shortBadge: String
) {
    ORIGINAL("original", "Original", "Original Script", "Orig"),
    LATIN("latin", "English / Romanized", "Latin (Phonetic)", "Roman"),
    TAMIL("tamil", "Tamil", "தமிழ்", "தமிழ்"),
    DEVANAGARI("devanagari", "Hindi / Devanagari", "हिन्दी", "हिन्दी"),
    TELUGU("telugu", "Telugu", "తెలుగు", "తెలుగు"),
    MALAYALAM("malayalam", "Malayalam", "മലയാളം", "മലയാളം"),
    KANNADA("kannada", "Kannada", "ಕನ್ನಡ", "ಕನ್ನಡ"),
    BENGALI("bengali", "Bengali", "বাংলা", "বাংলা"),
    GUJARATI("gujarati", "Gujarati", "ગુજરાતી", "ગુજરાતી"),
    PUNJABI("punjabi", "Punjabi", "ਪੰਜਾਬੀ", "ਪੰਜਾਬੀ"),
    CYRILLIC("cyrillic", "Cyrillic", "Русский", "Рус"),
    GREEK("greek", "Greek", "Ελληνικά", "Ελλ");

    companion object {
        fun fromId(id: String): TransliterationTarget {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ORIGINAL
        }
    }
}

object LyricsTransliterator {

    /**
     * Transliterates ONLY lyric text into the chosen target script.
     * Chords must never be passed here; this operates purely on lyrics syllables and lines.
     */
    fun transliterateLyric(lyric: String, target: TransliterationTarget): String {
        if (target == TransliterationTarget.ORIGINAL || lyric.isEmpty()) {
            return lyric
        }

        return when (target) {
            TransliterationTarget.LATIN -> toLatin(lyric)
            TransliterationTarget.TAMIL -> toBrahmicScript(lyric, ScriptBase.TAMIL)
            TransliterationTarget.DEVANAGARI -> toBrahmicScript(lyric, ScriptBase.DEVANAGARI)
            TransliterationTarget.TELUGU -> toBrahmicScript(lyric, ScriptBase.TELUGU)
            TransliterationTarget.MALAYALAM -> toBrahmicScript(lyric, ScriptBase.MALAYALAM)
            TransliterationTarget.KANNADA -> toBrahmicScript(lyric, ScriptBase.KANNADA)
            TransliterationTarget.BENGALI -> toBrahmicScript(lyric, ScriptBase.BENGALI)
            TransliterationTarget.GUJARATI -> toBrahmicScript(lyric, ScriptBase.GUJARATI)
            TransliterationTarget.PUNJABI -> toBrahmicScript(lyric, ScriptBase.GURMUKHI)
            TransliterationTarget.CYRILLIC -> toCyrillic(lyric)
            TransliterationTarget.GREEK -> toGreek(lyric)
            TransliterationTarget.ORIGINAL -> lyric
        }
    }

    // =========================================================================
    // 1. UNIVERSAL TO LATIN (ROMANIZATION)
    // =========================================================================

    private fun toLatin(text: String): String {
        val sb = StringBuilder(text.length * 2)
        var i = 0
        val len = text.length

        while (i < len) {
            val ch = text[i]

            when {
                // Tamil block (0x0B80 - 0x0BFF)
                ch in '\u0B80'..'\u0BFF' -> {
                    i = processTamilToLatin(text, i, sb)
                }

                // Devanagari block (0x0900 - 0x097F)
                ch in '\u0900'..'\u097F' -> {
                    i = processDevanagariToLatin(text, i, sb)
                }

                // Telugu block (0x0C00 - 0x0C7F)
                ch in '\u0C00'..'\u0C7F' -> {
                    i = processTeluguToLatin(text, i, sb)
                }

                // Malayalam block (0x0D00 - 0x0D7F)
                ch in '\u0D00'..'\u0D7F' -> {
                    i = processMalayalamToLatin(text, i, sb)
                }

                // Kannada block (0x0C80 - 0x0CFF)
                ch in '\u0C80'..'\u0CFF' -> {
                    i = processKannadaToLatin(text, i, sb)
                }

                // Bengali block (0x0980 - 0x09FF)
                ch in '\u0980'..'\u09FF' -> {
                    i = processBengaliToLatin(text, i, sb)
                }

                // Gujarati block (0x0A80 - 0x0AFF)
                ch in '\u0A80'..'\u0AFF' -> {
                    i = processGujaratiToLatin(text, i, sb)
                }

                // Gurmukhi / Punjabi block (0x0A00 - 0x0A7F)
                ch in '\u0A00'..'\u0A7F' -> {
                    i = processGurmukhiToLatin(text, i, sb)
                }

                // Cyrillic block (0x0400 - 0x04FF)
                ch in '\u0400'..'\u04FF' -> {
                    sb.append(cyrillicToLatin(ch))
                    i++
                }

                // Greek block (0x0370 - 0x03FF)
                ch in '\u0370'..'\u03FF' -> {
                    sb.append(greekToLatin(ch))
                    i++
                }

                // Korean Hangul Syllables (0xAC00 - 0xD7AF)
                ch in '\uAC00'..'\uD7AF' -> {
                    sb.append(hangulToLatin(ch))
                    i++
                }

                // Japanese Hiragana & Katakana (0x3040 - 0x30FF)
                ch in '\u3040'..'\u30FF' -> {
                    sb.append(kanaToLatin(ch))
                    i++
                }

                // Arabic (0x0600 - 0x06FF)
                ch in '\u0600'..'\u06FF' -> {
                    sb.append(arabicToLatin(ch))
                    i++
                }

                else -> {
                    sb.append(ch)
                    i++
                }
            }
        }

        return sb.toString()
    }

    // --- TAMIL TO LATIN ---
    private fun processTamilToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        var nextIdx = startIdx + 1

        // Independent vowels
        val indepVowel = TAMIL_INDEPENDENT_VOWELS[ch]
        if (indepVowel != null) {
            sb.append(indepVowel)
            return nextIdx
        }

        // Ayutha ezhuthu ஃ
        if (ch == '\u0B83') {
            sb.append("ah")
            return nextIdx
        }

        // Consonant
        val consonantBase = TAMIL_CONSONANTS[ch]
        if (consonantBase != null) {
            // Check following modifier (virama/pulli or dependent vowel)
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u0BCD') {
                    // Pulli / virama: Pure consonant without vowel
                    sb.append(consonantBase)
                    return nextIdx + 1
                }
                val depVowel = TAMIL_DEPENDENT_VOWELS[nextCh]
                if (depVowel != null) {
                    sb.append(consonantBase)
                    sb.append(depVowel)
                    return nextIdx + 1
                }
            }
            // Inherent 'a'
            sb.append(consonantBase)
            sb.append("a")
            return nextIdx
        }

        // Standalone dependent vowel or other
        val dep = TAMIL_DEPENDENT_VOWELS[ch]
        if (dep != null) {
            sb.append(dep)
        } else {
            sb.append(ch)
        }
        return nextIdx
    }

    private val TAMIL_INDEPENDENT_VOWELS = mapOf(
        '\u0B85' to "a",
        '\u0B86' to "aa",
        '\u0B87' to "i",
        '\u0B88' to "ee",
        '\u0B89' to "u",
        '\u0B8A' to "oo",
        '\u0B8E' to "e",
        '\u0B8F' to "ae",
        '\u0B90' to "ai",
        '\u0B92' to "o",
        '\u0B93' to "oa",
        '\u0B94' to "au"
    )

    private val TAMIL_DEPENDENT_VOWELS = mapOf(
        '\u0BBE' to "aa",
        '\u0BBF' to "i",
        '\u0BC0' to "ee",
        '\u0BC1' to "u",
        '\u0BC2' to "oo",
        '\u0BC6' to "e",
        '\u0BC7' to "ae",
        '\u0BC8' to "ai",
        '\u0BCA' to "o",
        '\u0BCB' to "oa",
        '\u0BCC' to "au"
    )

    private val TAMIL_CONSONANTS = mapOf(
        '\u0B95' to "k",
        '\u0B99' to "ng",
        '\u0B9A' to "s",
        '\u0B9C' to "j",
        '\u0B9E' to "ny",
        '\u0B9F' to "t",
        '\u0BA3' to "n",
        '\u0BA4' to "th",
        '\u0BA8' to "n",
        '\u0BA9' to "n",
        '\u0BAA' to "p",
        '\u0BAE' to "m",
        '\u0BAF' to "y",
        '\u0BB0' to "r",
        '\u0BB1' to "r",
        '\u0BB2' to "l",
        '\u0BB3' to "l",
        '\u0BB4' to "zh",
        '\u0BB5' to "v",
        '\u0BB6' to "sh",
        '\u0BB7' to "sh",
        '\u0BB8' to "s",
        '\u0BB9' to "h"
    )

    // --- DEVANAGARI (HINDI) TO LATIN ---
    private fun processDevanagariToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        var nextIdx = startIdx + 1

        val indep = DEVANAGARI_INDEPENDENT_VOWELS[ch]
        if (indep != null) {
            sb.append(indep)
            return nextIdx
        }

        if (ch == '\u0902' || ch == '\u0901') {
            sb.append("n")
            return nextIdx
        }
        if (ch == '\u0903') {
            sb.append("h")
            return nextIdx
        }

        val consonant = DEVANAGARI_CONSONANTS[ch]
        if (consonant != null) {
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u094D') {
                    // Halant (virama)
                    sb.append(consonant)
                    return nextIdx + 1
                }
                val dep = DEVANAGARI_DEPENDENT_VOWELS[nextCh]
                if (dep != null) {
                    sb.append(consonant)
                    sb.append(dep)
                    return nextIdx + 1
                }
            }
            sb.append(consonant)
            sb.append("a")
            return nextIdx
        }

        val dep = DEVANAGARI_DEPENDENT_VOWELS[ch]
        if (dep != null) {
            sb.append(dep)
        } else {
            sb.append(ch)
        }
        return nextIdx
    }

    private val DEVANAGARI_INDEPENDENT_VOWELS = mapOf(
        '\u0905' to "a", '\u0906' to "aa", '\u0907' to "i", '\u0908' to "ee",
        '\u0909' to "u", '\u090A' to "oo", '\u090B' to "ri", '\u090F' to "e",
        '\u0910' to "ai", '\u0913' to "o", '\u0914' to "au"
    )

    private val DEVANAGARI_DEPENDENT_VOWELS = mapOf(
        '\u093E' to "aa", '\u093F' to "i", '\u0940' to "ee", '\u0941' to "u",
        '\u0942' to "oo", '\u0943' to "ri", '\u0947' to "e", '\u0948' to "ai",
        '\u094B' to "o", '\u094C' to "au"
    )

    private val DEVANAGARI_CONSONANTS = mapOf(
        '\u0915' to "k", '\u0916' to "kh", '\u0917' to "g", '\u0918' to "gh", '\u0919' to "ng",
        '\u091A' to "ch", '\u091B' to "chh", '\u091C' to "j", '\u091D' to "jh", '\u091E' to "ny",
        '\u091F' to "t", '\u0920' to "th", '\u0921' to "d", '\u0922' to "dh", '\u0923' to "n",
        '\u0924' to "t", '\u0925' to "th", '\u0926' to "d", '\u0927' to "dh", '\u0928' to "n",
        '\u092A' to "p", '\u092B' to "ph", '\u092C' to "b", '\u092D' to "bh", '\u092E' to "m",
        '\u092F' to "y", '\u0930' to "r", '\u0932' to "l", '\u0933' to "l", '\u0935' to "v",
        '\u0936' to "sh", '\u0937' to "sh", '\u0938' to "s", '\u0939' to "h"
    )

    // --- GENERIC BRAHMIC SCRIPT CONVERSION TO LATIN ---
    private fun processBrahmicScript(
        text: String,
        startIdx: Int,
        sb: StringBuilder,
        indepMap: Map<Char, String>,
        depMap: Map<Char, String>,
        consMap: Map<Char, String>,
        virama: Char
    ): Int {
        val ch = text[startIdx]
        var nextIdx = startIdx + 1

        val indep = indepMap[ch]
        if (indep != null) {
            sb.append(indep)
            return nextIdx
        }

        val consonant = consMap[ch]
        if (consonant != null) {
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == virama) {
                    sb.append(consonant)
                    return nextIdx + 1
                }
                val dep = depMap[nextCh]
                if (dep != null) {
                    sb.append(consonant)
                    sb.append(dep)
                    return nextIdx + 1
                }
            }
            sb.append(consonant)
            sb.append("a")
            return nextIdx
        }

        val dep = depMap[ch]
        if (dep != null) {
            sb.append(dep)
        } else {
            sb.append(ch)
        }
        return nextIdx
    }

    // --- TELUGU TO LATIN ---
    private fun processTeluguToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        val offset = ch.code - 0x0C00
        val devChar = (0x0900 + offset).toChar()
        val indep = DEVANAGARI_INDEPENDENT_VOWELS[devChar]
        if (indep != null) { sb.append(indep); return startIdx + 1 }
        val dep = DEVANAGARI_DEPENDENT_VOWELS[devChar]
        val cons = DEVANAGARI_CONSONANTS[devChar]
        if (cons != null) {
            var nextIdx = startIdx + 1
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u0C4D') { sb.append(cons); return nextIdx + 1 }
                val nextDev = (0x0900 + (nextCh.code - 0x0C00)).toChar()
                val nextDep = DEVANAGARI_DEPENDENT_VOWELS[nextDev]
                if (nextDep != null) { sb.append(cons); sb.append(nextDep); return nextIdx + 1 }
            }
            sb.append(cons); sb.append("a")
            return nextIdx
        }
        if (dep != null) sb.append(dep) else sb.append(ch)
        return startIdx + 1
    }

    // --- MALAYALAM TO LATIN ---
    private fun processMalayalamToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        val offset = ch.code - 0x0D00
        val devChar = (0x0900 + offset).toChar()
        val indep = DEVANAGARI_INDEPENDENT_VOWELS[devChar]
        if (indep != null) { sb.append(indep); return startIdx + 1 }
        val dep = DEVANAGARI_DEPENDENT_VOWELS[devChar]
        val cons = DEVANAGARI_CONSONANTS[devChar]
        if (cons != null) {
            var nextIdx = startIdx + 1
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u0D4D') { sb.append(cons); return nextIdx + 1 }
                val nextDev = (0x0900 + (nextCh.code - 0x0D00)).toChar()
                val nextDep = DEVANAGARI_DEPENDENT_VOWELS[nextDev]
                if (nextDep != null) { sb.append(cons); sb.append(nextDep); return nextIdx + 1 }
            }
            sb.append(cons); sb.append("a")
            return nextIdx
        }
        if (dep != null) sb.append(dep) else sb.append(ch)
        return startIdx + 1
    }

    // --- KANNADA TO LATIN ---
    private fun processKannadaToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        val offset = ch.code - 0x0C80
        val devChar = (0x0900 + offset).toChar()
        val indep = DEVANAGARI_INDEPENDENT_VOWELS[devChar]
        if (indep != null) { sb.append(indep); return startIdx + 1 }
        val dep = DEVANAGARI_DEPENDENT_VOWELS[devChar]
        val cons = DEVANAGARI_CONSONANTS[devChar]
        if (cons != null) {
            var nextIdx = startIdx + 1
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u0CCD') { sb.append(cons); return nextIdx + 1 }
                val nextDev = (0x0900 + (nextCh.code - 0x0C80)).toChar()
                val nextDep = DEVANAGARI_DEPENDENT_VOWELS[nextDev]
                if (nextDep != null) { sb.append(cons); sb.append(nextDep); return nextIdx + 1 }
            }
            sb.append(cons); sb.append("a")
            return nextIdx
        }
        if (dep != null) sb.append(dep) else sb.append(ch)
        return startIdx + 1
    }

    // --- BENGALI TO LATIN ---
    private fun processBengaliToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        val offset = ch.code - 0x0980
        val devChar = (0x0900 + offset).toChar()
        val indep = DEVANAGARI_INDEPENDENT_VOWELS[devChar]
        if (indep != null) { sb.append(indep); return startIdx + 1 }
        val dep = DEVANAGARI_DEPENDENT_VOWELS[devChar]
        val cons = DEVANAGARI_CONSONANTS[devChar]
        if (cons != null) {
            var nextIdx = startIdx + 1
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u09CD') { sb.append(cons); return nextIdx + 1 }
                val nextDev = (0x0900 + (nextCh.code - 0x0980)).toChar()
                val nextDep = DEVANAGARI_DEPENDENT_VOWELS[nextDev]
                if (nextDep != null) { sb.append(cons); sb.append(nextDep); return nextIdx + 1 }
            }
            sb.append(cons); sb.append("a")
            return nextIdx
        }
        if (dep != null) sb.append(dep) else sb.append(ch)
        return startIdx + 1
    }

    // --- GUJARATI TO LATIN ---
    private fun processGujaratiToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        val offset = ch.code - 0x0A80
        val devChar = (0x0900 + offset).toChar()
        val indep = DEVANAGARI_INDEPENDENT_VOWELS[devChar]
        if (indep != null) { sb.append(indep); return startIdx + 1 }
        val dep = DEVANAGARI_DEPENDENT_VOWELS[devChar]
        val cons = DEVANAGARI_CONSONANTS[devChar]
        if (cons != null) {
            var nextIdx = startIdx + 1
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u0ACD') { sb.append(cons); return nextIdx + 1 }
                val nextDev = (0x0900 + (nextCh.code - 0x0A80)).toChar()
                val nextDep = DEVANAGARI_DEPENDENT_VOWELS[nextDev]
                if (nextDep != null) { sb.append(cons); sb.append(nextDep); return nextIdx + 1 }
            }
            sb.append(cons); sb.append("a")
            return nextIdx
        }
        if (dep != null) sb.append(dep) else sb.append(ch)
        return startIdx + 1
    }

    // --- GURMUKHI / PUNJABI TO LATIN ---
    private fun processGurmukhiToLatin(text: String, startIdx: Int, sb: StringBuilder): Int {
        val ch = text[startIdx]
        val offset = ch.code - 0x0A00
        val devChar = (0x0900 + offset).toChar()
        val indep = DEVANAGARI_INDEPENDENT_VOWELS[devChar]
        if (indep != null) { sb.append(indep); return startIdx + 1 }
        val dep = DEVANAGARI_DEPENDENT_VOWELS[devChar]
        val cons = DEVANAGARI_CONSONANTS[devChar]
        if (cons != null) {
            var nextIdx = startIdx + 1
            if (nextIdx < text.length) {
                val nextCh = text[nextIdx]
                if (nextCh == '\u0A4D') { sb.append(cons); return nextIdx + 1 }
                val nextDev = (0x0900 + (nextCh.code - 0x0A00)).toChar()
                val nextDep = DEVANAGARI_DEPENDENT_VOWELS[nextDev]
                if (nextDep != null) { sb.append(cons); sb.append(nextDep); return nextIdx + 1 }
            }
            sb.append(cons); sb.append("a")
            return nextIdx
        }
        if (dep != null) sb.append(dep) else sb.append(ch)
        return startIdx + 1
    }

    // --- CYRILLIC TO LATIN ---
    private fun cyrillicToLatin(ch: Char): String {
        return when (ch) {
            'А' -> "A"; 'а' -> "a"
            'Б' -> "B"; 'б' -> "b"
            'В' -> "V"; 'в' -> "v"
            'Г' -> "G"; 'г' -> "g"
            'Д' -> "D"; 'д' -> "d"
            'Е' -> "E"; 'е' -> "e"
            'Ё' -> "Yo"; 'ё' -> "yo"
            'Ж' -> "Zh"; 'ж' -> "zh"
            'З' -> "Z"; 'з' -> "z"
            'И' -> "I"; 'и' -> "i"
            'Й' -> "Y"; 'й' -> "y"
            'К' -> "K"; 'к' -> "k"
            'Л' -> "L"; 'л' -> "l"
            'М' -> "M"; 'м' -> "m"
            'Н' -> "N"; 'н' -> "n"
            'О' -> "O"; 'о' -> "o"
            'П' -> "P"; 'п' -> "p"
            'Р' -> "R"; 'р' -> "r"
            'С' -> "S"; 'с' -> "s"
            'Т' -> "T"; 'т' -> "t"
            'У' -> "U"; 'у' -> "u"
            'Ф' -> "F"; 'ф' -> "f"
            'Х' -> "Kh"; 'х' -> "kh"
            'Ц' -> "Ts"; 'ц' -> "ts"
            'Ч' -> "Ch"; 'ч' -> "ch"
            'Ш' -> "Sh"; 'ш' -> "sh"
            'Щ' -> "Shch"; 'щ' -> "shch"
            'Ъ', 'ъ' -> ""
            'Ы' -> "Y"; 'ы' -> "y"
            'Ь', 'ь' -> "'"
            'Э' -> "E"; 'э' -> "e"
            'Ю' -> "Yu"; 'ю' -> "yu"
            'Я' -> "Ya"; 'я' -> "ya"
            else -> ch.toString()
        }
    }

    // --- GREEK TO LATIN ---
    private fun greekToLatin(ch: Char): String {
        return when (ch) {
            'Α', 'ά' -> "a"; 'α' -> "a"
            'Β' -> "V"; 'β' -> "v"
            'Γ' -> "G"; 'γ' -> "g"
            'Δ' -> "D"; 'δ' -> "d"
            'Ε', 'έ' -> "e"; 'ε' -> "e"
            'Ζ' -> "Z"; 'ζ' -> "z"
            'Η', 'ή' -> "i"; 'η' -> "i"
            'Θ' -> "Th"; 'θ' -> "th"
            'Ι', 'ί', 'ϊ', 'ΐ' -> "i"; 'ι' -> "i"
            'Κ' -> "K"; 'κ' -> "k"
            'Λ' -> "L"; 'λ' -> "l"
            'Μ' -> "M"; 'μ' -> "m"
            'Ν' -> "N"; 'ν' -> "n"
            'Ξ' -> "X"; 'ξ' -> "x"
            'Ο', 'ό' -> "o"; 'ο' -> "o"
            'Π' -> "P"; 'π' -> "p"
            'Ρ' -> "R"; 'ρ' -> "r"
            'Σ', 'ς' -> "s"; 'σ' -> "s"
            'Τ' -> "T"; 'τ' -> "t"
            'Υ', 'ύ', 'ϋ', 'ΰ' -> "y"; 'υ' -> "y"
            'Φ' -> "F"; 'φ' -> "f"
            'Χ' -> "Ch"; 'χ' -> "ch"
            'Ψ' -> "Ps"; 'ψ' -> "ps"
            'Ω', 'ώ' -> "o"; 'ω' -> "o"
            else -> ch.toString()
        }
    }

    // --- HANGUL TO LATIN ---
    private val HANGUL_INITIALS = arrayOf(
        "g", "kk", "n", "d", "tt", "r", "m", "b", "pp", "s", "ss", "",
        "j", "jj", "ch", "k", "t", "p", "h"
    )
    private val HANGUL_VOWELS = arrayOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o", "wa", "wae", "oe",
        "yo", "u", "wo", "we", "wi", "yu", "eu", "ui", "i"
    )
    private val HANGUL_FINALS = arrayOf(
        "", "k", "k", "ks", "n", "nj", "nh", "d", "l", "lg", "lm", "lb",
        "ls", "lt", "lp", "lh", "m", "b", "bs", "s", "ss", "ng", "j", "ch",
        "k", "t", "p", "h"
    )

    private fun hangulToLatin(ch: Char): String {
        val code = ch.code - 0xAC00
        val initialIdx = code / (21 * 28)
        val vowelIdx = (code % (21 * 28)) / 28
        val finalIdx = code % 28

        return HANGUL_INITIALS.getOrElse(initialIdx) { "" } +
                HANGUL_VOWELS.getOrElse(vowelIdx) { "" } +
                HANGUL_FINALS.getOrElse(finalIdx) { "" }
    }

    // --- JAPANESE KANA TO LATIN ---
    private fun kanaToLatin(ch: Char): String {
        return JAPANESE_KANA_MAP[ch] ?: ch.toString()
    }

    private val JAPANESE_KANA_MAP = mapOf(
        'あ' to "a", 'い' to "i", 'う' to "u", 'え' to "e", 'お' to "o",
        'か' to "ka", 'き' to "ki", 'く' to "ku", 'け' to "ke", 'こ' to "ko",
        'さ' to "sa", 'し' to "shi", 'す' to "su", 'せ' to "se", 'そ' to "so",
        'た' to "ta", 'ち' to "chi", 'つ' to "tsu", 'て' to "te", 'と' to "to",
        'な' to "na", 'に' to "ni", 'ぬ' to "nu", 'ね' to "ne", 'の' to "no",
        'は' to "ha", 'ひ' to "hi", 'ふ' to "fu", 'へ' to "he", 'ほ' to "ho",
        'ま' to "ma", 'み' to "mi", 'む' to "mu", 'め' to "me", 'も' to "mo",
        'や' to "ya", 'ゆ' to "yu", 'よ' to "yo",
        'ら' to "ra", 'り' to "ri", 'る' to "ru", 'れ' to "re", 'ろ' to "ro",
        'わ' to "wa", 'を' to "wo", 'ん' to "n",
        // Katakana
        'ア' to "a", 'イ' to "i", 'ウ' to "u", 'エ' to "e", 'オ' to "o",
        'カ' to "ka", 'キ' to "ki", 'ク' to "ku", 'ケ' to "ke", 'コ' to "ko",
        'サ' to "sa", 'シ' to "shi", 'ス' to "su", 'セ' to "se", 'ソ' to "so",
        'タ' to "ta", 'チ' to "chi", 'ツ' to "tsu", 'テ' to "te", 'ト' to "to",
        'ナ' to "na", 'ニ' to "ni", 'ヌ' to "nu", 'ネ' to "ne", 'ノ' to "no",
        'ハ' to "ha", 'ヒ' to "hi", 'フ' to "fu", 'ヘ' to "he", 'ホ' to "ho",
        'マ' to "ma", 'ミ' to "mi", 'ム' to "mu", 'メ' to "me", 'モ' to "mo",
        'ヤ' to "ya", 'ユ' to "yu", 'ヨ' to "yo",
        'ラ' to "ra", 'リ' to "ri", 'ル' to "ru", 'レ' to "re", 'ロ' to "ro",
        'ワ' to "wa", 'ヲ' to "wo", 'ン' to "n"
    )

    private fun arabicToLatin(ch: Char): String {
        return when (ch) {
            '\u0627' -> "a"; '\u0628' -> "b"; '\u062A' -> "t"; '\u062B' -> "th"
            '\u062C' -> "j"; '\u062D' -> "h"; '\u062E' -> "kh"; '\u062F' -> "d"
            '\u0630' -> "dh"; '\u0631' -> "r"; '\u0632' -> "z"; '\u0633' -> "s"
            '\u0634' -> "sh"; '\u0635' -> "s"; '\u0636' -> "d"; '\u0637' -> "t"
            '\u0638' -> "z"; '\u0639' -> "'"; '\u063A' -> "gh"; '\u0641' -> "f"
            '\u0642' -> "q"; '\u0643' -> "k"; '\u0644' -> "l"; '\u0645' -> "m"
            '\u0646' -> "n"; '\u0647' -> "h"; '\u0648' -> "w"; '\u064A' -> "y"
            else -> ch.toString()
        }
    }

    // =========================================================================
    // 2. INDIC SCRIPT TO INDIC SCRIPT (BRAHMIC RE-ENCODING)
    // =========================================================================

    private enum class ScriptBase(val baseCode: Int) {
        DEVANAGARI(0x0900),
        BENGALI(0x0980),
        GURMUKHI(0x0A00),
        GUJARATI(0x0A80),
        TAMIL(0x0B80),
        TELUGU(0x0C00),
        KANNADA(0x0C80),
        MALAYALAM(0x0D00)
    }

    private fun toBrahmicScript(text: String, targetBase: ScriptBase): String {
        val sb = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            val srcBase = getScriptBase(ch)
            if (srcBase != null) {
                val offset = ch.code - srcBase.baseCode
                // Special mapping for Tamil script which lacks aspirated consonants
                if (targetBase == ScriptBase.TAMIL) {
                    val tamilMapped = mapToTamilOffset(offset)
                    sb.append((ScriptBase.TAMIL.baseCode + tamilMapped).toChar())
                } else {
                    sb.append((targetBase.baseCode + offset).toChar())
                }
            } else {
                sb.append(ch)
            }
            i++
        }
        return sb.toString()
    }

    private fun getScriptBase(ch: Char): ScriptBase? {
        val code = ch.code
        return when (code) {
            in 0x0900..0x097F -> ScriptBase.DEVANAGARI
            in 0x0980..0x09FF -> ScriptBase.BENGALI
            in 0x0A00..0x0A7F -> ScriptBase.GURMUKHI
            in 0x0A80..0x0AFF -> ScriptBase.GUJARATI
            in 0x0B80..0x0BFF -> ScriptBase.TAMIL
            in 0x0C00..0x0C7F -> ScriptBase.TELUGU
            in 0x0C80..0x0CFF -> ScriptBase.KANNADA
            in 0x0D00..0x0D7F -> ScriptBase.MALAYALAM
            else -> null
        }
    }

    /**
     * Maps Devanagari/Telugu/Malayalam consonantal offsets to Tamil equivalents.
     */
    private fun mapToTamilOffset(offset: Int): Int {
        return when (offset) {
            // Aspirated & voiced velars: kha (0x16), ga (0x17), gha (0x18) -> ka (0x15)
            0x16, 0x17, 0x18 -> 0x15
            // Palatals: chha (0x1B), jha (0x1D) -> cha/sa (0x1A)
            0x1B, 0x1D -> 0x1A
            // Retroflex: tha (0x20), da (0x21), dha (0x22) -> ta (0x1F)
            0x20, 0x21, 0x22 -> 0x1F
            // Dentals: tha (0x25), da (0x26), dha (0x27) -> tha (0x24)
            0x25, 0x26, 0x27 -> 0x24
            // Labials: pha (0x2B), ba (0x2C), bha (0x2D) -> pa (0x2A)
            0x2B, 0x2C, 0x2D -> 0x2A
            else -> offset
        }
    }

    private fun toCyrillic(text: String): String {
        // Transliterates Latin text to Cyrillic
        val latinToCyr = mapOf(
            "shch" to "щ", "yo" to "ё", "zh" to "ж", "ts" to "ц",
            "ch" to "ч", "sh" to "ш", "yu" to "ю", "ya" to "я",
            "kh" to "х", "a" to "а", "b" to "б", "v" to "в",
            "g" to "г", "d" to "д", "e" to "е", "z" to "з",
            "i" to "и", "k" to "к", "l" to "л", "m" to "м",
            "n" to "н", "o" to "о", "p" to "п", "r" to "р",
            "s" to "с", "t" to "т", "u" to "у", "f" to "ф",
            "y" to "ы"
        )
        var res = text.lowercase()
        for ((latin, cyr) in latinToCyr) {
            res = res.replace(latin, cyr)
        }
        return res
    }

    private fun toGreek(text: String): String {
        val latinToGreek = mapOf(
            "th" to "θ", "ch" to "χ", "ps" to "ψ",
            "a" to "α", "b" to "β", "g" to "γ", "d" to "δ",
            "e" to "ε", "z" to "ζ", "i" to "ι", "k" to "κ",
            "l" to "λ", "m" to "μ", "n" to "ν", "x" to "ξ",
            "o" to "ο", "p" to "π", "r" to "ρ", "s" to "σ",
            "t" to "τ", "y" to "υ", "f" to "φ"
        )
        var res = text.lowercase()
        for ((latin, grk) in latinToGreek) {
            res = res.replace(latin, grk)
        }
        return res
    }
}
