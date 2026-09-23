package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.chord.GuitarChordVoicing

@Composable
fun GuitarChordCard(
    voicing: GuitarChordVoicing,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    highlightColor: Color = MaterialTheme.colorScheme.primary,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    showTitle: Boolean = true,
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
            .testTag("guitar_chord_card_${voicing.chordName}")
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (showTitle) {
                Text(
                    text = voicing.chordName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = if (size > 120.dp) 18.sp else 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            GuitarFretboardDiagram(
                voicing = voicing,
                diagramWidth = size,
                diagramHeight = size * 1.25f,
                dotColor = highlightColor,
                lineColor = textColor.copy(alpha = 0.75f),
                nutColor = textColor
            )
        }
    }
}

@Composable
fun GuitarFretboardDiagram(
    voicing: GuitarChordVoicing,
    diagramWidth: Dp,
    diagramHeight: Dp,
    dotColor: Color = MaterialTheme.colorScheme.primary,
    lineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
    nutColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .width(diagramWidth)
            .height(diagramHeight)
    ) {
        val numStrings = 6
        val numFrets = 4

        // Layout measurements
        val topPadding = size.height * 0.18f // room for O and X markers
        val bottomPadding = size.height * 0.08f
        val leftPadding = size.width * 0.22f // room for base fret text e.g. "3fr"
        val rightPadding = size.width * 0.15f

        val fretboardWidth = size.width - leftPadding - rightPadding
        val fretboardHeight = size.height - topPadding - bottomPadding

        val stringSpacing = fretboardWidth / (numStrings - 1)
        val fretSpacing = fretboardHeight / numFrets

        val isNut = voicing.baseFret == 1

        // 1. Draw Nut or top fret line
        val nutY = topPadding
        val nutThickness = if (isNut) 5.dp.toPx() else 2.dp.toPx()
        drawLine(
            color = if (isNut) nutColor else lineColor,
            start = Offset(leftPadding, nutY),
            end = Offset(leftPadding + fretboardWidth, nutY),
            strokeWidth = nutThickness
        )

        // Draw Base Fret indicator if not nut (e.g. "3fr")
        if (!isNut) {
            // Fret offset indicator drawn on left
            val textPaint = android.graphics.Paint().apply {
                color = nutColor.hashCode()
                textSize = size.width * 0.14f
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${voicing.baseFret}fr",
                leftPadding * 0.12f,
                nutY + fretSpacing * 0.65f,
                textPaint
            )
        }

        // 2. Draw fret horizontal lines
        for (f in 1..numFrets) {
            val y = topPadding + f * fretSpacing
            drawLine(
                color = lineColor,
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + fretboardWidth, y),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // 3. Draw vertical string lines (low E (6) to high E (1))
        for (s in 0 until numStrings) {
            val x = leftPadding + s * stringSpacing
            // Realistic string thickness: string 6 is thicker than string 1
            val stringWidth = when (s) {
                0 -> 2.5.dp.toPx()
                1 -> 2.2.dp.toPx()
                2 -> 1.8.dp.toPx()
                3 -> 1.5.dp.toPx()
                4 -> 1.2.dp.toPx()
                else -> 1.0.dp.toPx()
            }
            drawLine(
                color = lineColor,
                start = Offset(x, nutY),
                end = Offset(x, topPadding + fretboardHeight),
                strokeWidth = stringWidth
            )
        }

        // 4. Draw Barre if any
        voicing.barres.forEach { barreFret ->
            if (barreFret in 1..numFrets) {
                val barreY = topPadding + (barreFret - 0.5f) * fretSpacing
                // Find first and last string with this fret
                var firstString = -1
                var lastString = -1
                for (s in 0 until numStrings) {
                    val fVal = voicing.frets.getOrNull(s) ?: -1
                    if (fVal >= barreFret) {
                        if (firstString == -1) firstString = s
                        lastString = s
                    }
                }
                if (firstString != -1 && lastString > firstString) {
                    val startX = leftPadding + firstString * stringSpacing
                    val endX = leftPadding + lastString * stringSpacing
                    val barreHeight = fretSpacing * 0.45f
                    drawRoundRect(
                        color = dotColor,
                        topLeft = Offset(startX - barreHeight / 2, barreY - barreHeight / 2),
                        size = Size(endX - startX + barreHeight, barreHeight),
                        cornerRadius = CornerRadius(barreHeight / 2, barreHeight / 2)
                    )
                }
            }
        }

        // 5. Draw Open (O), Mute (X), and Pressed Dots
        val dotRadius = stringSpacing * 0.36f
        val markerPaint = android.graphics.Paint().apply {
            color = nutColor.hashCode()
            textSize = size.width * 0.13f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val dotTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = dotRadius * 1.15f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        for (s in 0 until numStrings) {
            val fret = voicing.frets.getOrNull(s) ?: -1
            val finger = voicing.fingers.getOrNull(s) ?: 0
            val x = leftPadding + s * stringSpacing

            when {
                // Muted string (X)
                fret == -1 -> {
                    drawContext.canvas.nativeCanvas.drawText("×", x, topPadding - 4.dp.toPx(), markerPaint)
                }
                // Open string (O)
                fret == 0 -> {
                    drawCircle(
                        color = nutColor,
                        radius = stringSpacing * 0.22f,
                        center = Offset(x, topPadding - 8.dp.toPx()),
                        style = Stroke(width = 1.8.dp.toPx())
                    )
                }
                // Pressed fret dot (1..numFrets)
                fret in 1..numFrets -> {
                    // Check if already covered by barre
                    val isCoveredByBarre = voicing.barres.contains(fret) && finger == 1
                    val y = topPadding + (fret - 0.5f) * fretSpacing

                    if (!isCoveredByBarre) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        // If finger number available, draw inside the dot
                        if (finger in 1..4 && diagramWidth > 70.dp) {
                            drawContext.canvas.nativeCanvas.drawText(
                                finger.toString(),
                                x,
                                y + dotRadius * 0.38f,
                                dotTextPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
