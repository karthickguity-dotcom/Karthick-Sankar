package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.chord.PianoChordVoicing

@Composable
fun PianoChordCard(
    voicing: PianoChordVoicing,
    modifier: Modifier = Modifier,
    width: Dp = 140.dp,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showTitle: Boolean = true,
    showNotesSubtitle: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("piano_chord_card_${voicing.chordName}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showTitle) {
                Text(
                    text = voicing.chordName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (width > 160.dp) 18.sp else 14.sp
                )
                if (showNotesSubtitle && voicing.notes.isNotEmpty()) {
                    Text(
                        text = voicing.notes.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.75f),
                        fontSize = if (width > 160.dp) 11.sp else 9.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            PianoKeyboardDiagram(
                voicing = voicing,
                diagramWidth = width,
                diagramHeight = width * 0.48f,
                highlightColor = highlightColor,
                keyBorderColor = textColor.copy(alpha = 0.4f),
                whiteKeyColor = Color(0xFFFCFDFD),
                blackKeyColor = Color(0xFF1E293B)
            )
        }
    }
}

@Composable
fun PianoKeyboardDiagram(
    voicing: PianoChordVoicing,
    diagramWidth: Dp,
    diagramHeight: Dp,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    keyBorderColor: Color = Color.Gray,
    whiteKeyColor: Color = Color.White,
    blackKeyColor: Color = Color(0xFF1E293B),
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(diagramWidth)
            .height(diagramHeight)
    ) {
        // 14 White keys representing two octaves: C3 to B4
        // Semitone mapping for 14 white keys:
        val whiteKeySemitones = intArrayOf(
            0, 2, 4, 5, 7, 9, 11,   // Octave 1: C, D, E, F, G, A, B
            12, 14, 16, 17, 19, 21, 23 // Octave 2: C, D, E, F, G, A, B
        )
        val numWhiteKeys = whiteKeySemitones.size
        val whiteKeyWidth = size.width / numWhiteKeys
        val whiteKeyHeight = size.height

        val blackKeyWidth = whiteKeyWidth * 0.62f
        val blackKeyHeight = whiteKeyHeight * 0.62f

        // Draw 14 White Keys
        for (i in 0 until numWhiteKeys) {
            val semitone = whiteKeySemitones[i]
            val isPressed = voicing.semitoneOffsets.contains(semitone)
            val keyX = i * whiteKeyWidth

            // Fill white key
            drawRoundRect(
                color = if (isPressed) highlightColor.copy(alpha = 0.32f) else whiteKeyColor,
                topLeft = Offset(keyX, 0f),
                size = Size(whiteKeyWidth, whiteKeyHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // White key border
            drawRoundRect(
                color = keyBorderColor,
                topLeft = Offset(keyX, 0f),
                size = Size(whiteKeyWidth, whiteKeyHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = 1.dp.toPx())
            )

            // If pressed, draw indicator circle near bottom of key
            if (isPressed) {
                drawCircle(
                    color = highlightColor,
                    radius = whiteKeyWidth * 0.28f,
                    center = Offset(keyX + whiteKeyWidth / 2f, whiteKeyHeight * 0.82f)
                )
            }
        }

        // Draw Black Keys
        // Black key indices relative to white keys:
        // C# is between white key 0 and 1
        // D# is between white key 1 and 2
        // F# is between white key 3 and 4
        // G# is between white key 4 and 5
        // A# is between white key 5 and 6
        // Repeated for 2nd octave (+7 white key offset, +12 semitones)
        data class BlackKeyDef(val whiteKeyIndex: Int, val semitone: Int)
        val blackKeys = listOf(
            BlackKeyDef(0, 1),   // C#1
            BlackKeyDef(1, 3),   // D#1
            BlackKeyDef(3, 6),   // F#1
            BlackKeyDef(4, 8),   // G#1
            BlackKeyDef(5, 10),  // A#1
            BlackKeyDef(7, 13),  // C#2
            BlackKeyDef(8, 15),  // D#2
            BlackKeyDef(10, 18), // F#2
            BlackKeyDef(11, 20), // G#2
            BlackKeyDef(12, 22)  // A#2
        )

        blackKeys.forEach { bk ->
            val isPressed = voicing.semitoneOffsets.contains(bk.semitone)
            // Centered on the dividing line between whiteKeyIndex and whiteKeyIndex + 1
            val bkX = (bk.whiteKeyIndex + 1) * whiteKeyWidth - blackKeyWidth / 2f

            drawRoundRect(
                color = if (isPressed) highlightColor else blackKeyColor,
                topLeft = Offset(bkX, 0f),
                size = Size(blackKeyWidth, blackKeyHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // Outline for definition
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.4f),
                topLeft = Offset(bkX, 0f),
                size = Size(blackKeyWidth, blackKeyHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = 0.8.dp.toPx())
            )

            if (isPressed) {
                drawCircle(
                    color = Color.White,
                    radius = blackKeyWidth * 0.22f,
                    center = Offset(bkX + blackKeyWidth / 2f, blackKeyHeight * 0.72f)
                )
            }
        }
    }
}
