package com.example.data.model

enum class SectionType {
    VERSE,
    CHORUS,
    BRIDGE,
    INTRO,
    OUTRO,
    PRE_CHORUS,
    GENERAL
}

enum class LineType {
    CHORD_LYRIC,
    SECTION_HEADER,
    COMMENT,
    BLANK
}

data class ChordLyricPair(
    val chord: String? = null,
    val lyric: String = ""
)

data class ChordProLine(
    val type: LineType,
    val text: String = "",
    val pairs: List<ChordLyricPair> = emptyList()
)

data class SongSection(
    val title: String = "",
    val type: SectionType = SectionType.GENERAL,
    val lines: List<ChordProLine> = emptyList()
)

data class ParsedSong(
    val title: String,
    val artist: String = "",
    val key: String = "",
    val capo: Int = 0,
    val tempo: String = "",
    val timeSignature: String = "",
    val sections: List<SongSection> = emptyList()
)
