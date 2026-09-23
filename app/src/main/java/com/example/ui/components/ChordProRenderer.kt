package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChordLyricPair
import com.example.data.model.ChordProLine
import com.example.data.model.LineType
import com.example.data.model.ParsedSong
import com.example.data.model.SectionType
import com.example.data.model.SongSection
import com.example.data.parser.AccidentalMode
import com.example.data.parser.Transposer

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChordProSongView(
    parsedSong: ParsedSong,
    transposeSemitones: Int,
    lyricsFontSize: Float,
    chordFontSize: Float,
    lineSpacing: Float,
    chordColor: Color,
    lyricsColor: Color,
    modifier: Modifier = Modifier,
    accidentalMode: AccidentalMode = AccidentalMode.SHARP,
    onChordClick: ((String) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        parsedSong.sections.forEachIndexed { sIdx, section ->
            // Render section header if title is present
            if (section.title.isNotBlank()) {
                SectionHeaderBadge(
                    title = section.title,
                    type = section.type,
                    modifier = Modifier.padding(top = if (sIdx > 0) (lineSpacing * 2.5f).dp else 8.dp, bottom = 8.dp)
                )
            }

            section.lines.forEach { line ->
                when (line.type) {
                    LineType.BLANK -> {
                        Spacer(modifier = Modifier.height((lyricsFontSize * 0.7f).dp))
                    }
                    LineType.COMMENT -> {
                        CommentBadge(
                            comment = line.text,
                            lyricsFontSize = lyricsFontSize,
                            lyricsColor = lyricsColor.copy(alpha = 0.8f),
                            modifier = Modifier.padding(vertical = (lineSpacing * 0.5f).dp)
                        )
                    }
                    LineType.SECTION_HEADER -> {
                        SectionHeaderBadge(
                            title = line.text,
                            type = section.type,
                            modifier = Modifier.padding(top = (lineSpacing * 2f).dp, bottom = 8.dp)
                        )
                    }
                    LineType.CHORD_LYRIC -> {
                        val hasChords = line.pairs.any { !it.chord.isNullOrBlank() }
                        if (hasChords) {
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = lineSpacing.dp)
                            ) {
                                line.pairs.forEach { pair ->
                                    ChordLyricCell(
                                        pair = pair,
                                        transposeSemitones = transposeSemitones,
                                        accidentalMode = accidentalMode,
                                        chordFontSize = chordFontSize,
                                        lyricsFontSize = lyricsFontSize,
                                        chordColor = chordColor,
                                        lyricsColor = lyricsColor,
                                        onChordClick = onChordClick
                                    )
                                }
                            }
                        } else {
                            // Pure lyrics line without any chords
                            val fullText = line.pairs.joinToString("") { it.lyric }.ifBlank { line.text }
                            Text(
                                text = fullText,
                                fontSize = lyricsFontSize.sp,
                                color = lyricsColor,
                                lineHeight = (lyricsFontSize * 1.4f).sp,
                                modifier = Modifier.padding(bottom = lineSpacing.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChordLyricCell(
    pair: ChordLyricPair,
    transposeSemitones: Int,
    accidentalMode: AccidentalMode,
    chordFontSize: Float,
    lyricsFontSize: Float,
    chordColor: Color,
    lyricsColor: Color,
    modifier: Modifier = Modifier,
    onChordClick: ((String) -> Unit)? = null
) {
    val transposedChord = pair.chord?.let {
        Transposer.transposeChord(it, transposeSemitones, accidentalMode)
    }

    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        // Chord above
        if (!transposedChord.isNullOrBlank()) {
            Text(
                text = transposedChord,
                color = chordColor,
                fontSize = chordFontSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                lineHeight = (chordFontSize * 1.2f).sp,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .then(
                        if (onChordClick != null) {
                            Modifier.clickable { onChordClick(transposedChord) }
                        } else {
                            Modifier
                        }
                    )
            )
        } else {
            // Invisible placeholder so lyrics in the same measure align properly
            Spacer(modifier = Modifier.height((chordFontSize * 1.2f).dp))
        }

        // Lyric below
        val lyricText = if (pair.lyric.isEmpty() && !transposedChord.isNullOrBlank()) {
            // Give chord breathing room if lyrics are empty at end of phrase
            "   "
        } else {
            pair.lyric
        }

        Text(
            text = lyricText,
            color = lyricsColor,
            fontSize = lyricsFontSize.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = (lyricsFontSize * 1.3f).sp
        )
    }
}

@Composable
fun SectionHeaderBadge(
    title: String,
    type: SectionType,
    modifier: Modifier = Modifier
) {
    val (badgeBg, badgeText) = when (type) {
        SectionType.CHORUS -> Pair(Color(0xFF818CF8).copy(alpha = 0.22f), Color(0xFFA5B4FC))
        SectionType.BRIDGE -> Pair(Color(0xFFF59E0B).copy(alpha = 0.22f), Color(0xFFFCD34D))
        SectionType.VERSE -> Pair(Color(0xFF10B981).copy(alpha = 0.22f), Color(0xFF6EE7B7))
        SectionType.INTRO, SectionType.OUTRO -> Pair(Color(0xFF06B6D4).copy(alpha = 0.22f), Color(0xFF67E8F9))
        else -> Pair(Color(0xFF64748B).copy(alpha = 0.22f), Color(0xFFCBD5E1))
    }

    Surface(
        color = badgeBg,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = title.uppercase(),
            color = badgeText,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun CommentBadge(
    comment: String,
    lyricsFontSize: Float,
    lyricsColor: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = "— $comment —",
        fontSize = (lyricsFontSize * 0.85f).sp,
        fontStyle = FontStyle.Italic,
        color = lyricsColor,
        fontWeight = FontWeight.Medium,
        modifier = modifier
    )
}
