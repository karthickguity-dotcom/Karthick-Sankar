package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.parser.AccidentalMode
import com.example.util.chord.ChordDatabase
import com.example.util.chord.InstrumentType

@Composable
fun ChordDiagramStrip(
    chords: List<String>,
    selectedInstrument: InstrumentType,
    onInstrumentChanged: (InstrumentType) -> Unit,
    chordColor: Color,
    textColor: Color,
    accidentalMode: AccidentalMode,
    modifier: Modifier = Modifier
) {
    var inspectedChord by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(vertical = 6.dp)
            .testTag("chord_diagram_strip")
    ) {
        // Selector bar: Instrument chips (Guitar / Piano / Hide)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Chords:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.8f)
                )

                FilterChip(
                    selected = selectedInstrument == InstrumentType.PIANO,
                    onClick = {
                        val next = if (selectedInstrument == InstrumentType.PIANO) InstrumentType.NONE else InstrumentType.PIANO
                        onInstrumentChanged(next)
                    },
                    label = { Text("🎹 Piano", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("instrument_chip_piano"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                FilterChip(
                    selected = selectedInstrument == InstrumentType.GUITAR,
                    onClick = {
                        val next = if (selectedInstrument == InstrumentType.GUITAR) InstrumentType.NONE else InstrumentType.GUITAR
                        onInstrumentChanged(next)
                    },
                    label = { Text("🎸 Guitar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("instrument_chip_guitar"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            if (selectedInstrument != InstrumentType.NONE) {
                IconButton(
                    onClick = { onInstrumentChanged(InstrumentType.NONE) },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("instrument_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hide Chord Diagrams",
                        tint = textColor.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Animated expansion of diagrams
        AnimatedVisibility(
            visible = selectedInstrument != InstrumentType.NONE && chords.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                chords.forEach { chordName ->
                    when (selectedInstrument) {
                        InstrumentType.GUITAR -> {
                            val voicing = remember(chordName) {
                                ChordDatabase.getGuitarChord(chordName)
                            }
                            GuitarChordCard(
                                voicing = voicing,
                                size = 88.dp,
                                highlightColor = chordColor,
                                textColor = textColor,
                                onClick = { inspectedChord = chordName }
                            )
                        }
                        InstrumentType.PIANO -> {
                            val voicing = remember(chordName, accidentalMode) {
                                ChordDatabase.getPianoChord(chordName, accidentalMode)
                            }
                            PianoChordCard(
                                voicing = voicing,
                                width = 126.dp,
                                highlightColor = chordColor,
                                textColor = textColor,
                                onClick = { inspectedChord = chordName }
                            )
                        }
                        InstrumentType.NONE, InstrumentType.BOTH -> {}
                    }
                }
            }
        }
    }

    // Modal inspection dialog for clicked chord
    inspectedChord?.let { chordName ->
        ChordDetailDialog(
            chordName = chordName,
            initialInstrument = if (selectedInstrument == InstrumentType.NONE || selectedInstrument == InstrumentType.BOTH) InstrumentType.GUITAR else selectedInstrument,
            chordColor = chordColor,
            accidentalMode = accidentalMode,
            onDismiss = { inspectedChord = null }
        )
    }
}

@Composable
fun ChordDetailDialog(
    chordName: String,
    initialInstrument: InstrumentType,
    chordColor: Color,
    accidentalMode: AccidentalMode,
    onDismiss: () -> Unit
) {
    var instrument by remember {
        mutableStateOf(if (initialInstrument == InstrumentType.NONE || initialInstrument == InstrumentType.BOTH) InstrumentType.PIANO else initialInstrument)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("chord_detail_close")) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = chordName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Instrument Switcher within Dialog: Piano first, then Guitar
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = instrument == InstrumentType.PIANO,
                        onClick = { instrument = InstrumentType.PIANO },
                        label = { Text("🎹", fontSize = 14.sp) }
                    )
                    FilterChip(
                        selected = instrument == InstrumentType.GUITAR,
                        onClick = { instrument = InstrumentType.GUITAR },
                        label = { Text("🎸", fontSize = 14.sp) }
                    )
                }
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                when (instrument) {
                    InstrumentType.GUITAR -> {
                        val voicing = remember(chordName) {
                            ChordDatabase.getGuitarChord(chordName)
                        }
                        GuitarChordCard(
                            voicing = voicing,
                            size = 140.dp,
                            highlightColor = chordColor,
                            showTitle = false
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Standard Tuning (E A D G B E)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (voicing.baseFret > 1) {
                            Text(
                                text = "Position starts at Fret ${voicing.baseFret}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    InstrumentType.PIANO -> {
                        val voicing = remember(chordName, accidentalMode) {
                            ChordDatabase.getPianoChord(chordName, accidentalMode)
                        }
                        PianoChordCard(
                            voicing = voicing,
                            width = 220.dp,
                            highlightColor = chordColor,
                            showTitle = false,
                            showNotesSubtitle = false
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Notes: ${voicing.notes.joinToString(" • ")}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    InstrumentType.NONE, InstrumentType.BOTH -> {}
                }
            }
        }
    )
}
