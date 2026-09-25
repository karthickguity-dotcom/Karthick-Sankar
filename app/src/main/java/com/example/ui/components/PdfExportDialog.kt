package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.parser.AccidentalMode
import com.example.util.chord.InstrumentType
import com.example.util.pdf.PdfExportOptions
import com.example.util.pdf.PdfFontSizeMode
import com.example.util.pdf.PdfPageSize

@Composable
fun PdfExportDialog(
    songTitle: String,
    currentKey: String,
    transposeSemitones: Int,
    accidentalMode: AccidentalMode,
    isExporting: Boolean,
    onPrint: (PdfExportOptions) -> Unit,
    onShare: (PdfExportOptions) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPageSize by remember { mutableStateOf(PdfPageSize.A4) }
    var selectedFontSize by remember { mutableStateOf(PdfFontSizeMode.STANDARD) }
    var selectedChordColorIndex by remember { mutableStateOf(0) }
    var selectedChordDiagramInstrument by remember { mutableStateOf(InstrumentType.BOTH) }
    var includeMetadata by remember { mutableStateOf(true) }
    var includeFooter by remember { mutableStateOf(true) }

    val chordColors = remember {
        listOf(
            Pair("Amber", 0xFFB45309.toInt()),
            Pair("Royal Blue", 0xFF2563EB.toInt()),
            Pair("Black", 0xFF111827.toInt()),
            Pair("Crimson", 0xFFBE123C.toInt())
        )
    }

    fun buildOptions(): PdfExportOptions {
        return PdfExportOptions(
            pageSize = selectedPageSize,
            fontSizeMode = selectedFontSize,
            chordColor = chordColors[selectedChordColorIndex].second,
            includeMetadata = includeMetadata,
            includeFooter = includeFooter,
            chordDiagramInstrument = selectedChordDiagramInstrument
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Export & Print PDF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = songTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("pdf_dialog_close")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Key and Transpose Confirmation Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Current Transposition Applied",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentKey.isNotBlank()) "Key: $currentKey" else "Default Chords",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                if (transposeSemitones != 0) {
                                    val sign = if (transposeSemitones > 0) "+$transposeSemitones" else "$transposeSemitones"
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "($sign)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = accidentalMode.symbol,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Page Paper Size
                Text(
                    text = "Page Size",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PdfPageSize.entries.forEach { size ->
                        val isSelected = selectedPageSize == size
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedPageSize = size },
                            label = { Text(size.name, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("pdf_size_${size.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Font Size / Density
                Text(
                    text = "Print Font Size",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PdfFontSizeMode.entries.forEach { mode ->
                        val isSelected = selectedFontSize == mode
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFontSize = mode },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.weight(1f).testTag("pdf_font_${mode.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chord Color Selection
                Text(
                    text = "Chord Ink Color",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chordColors.forEachIndexed { index, pair ->
                        val isSelected = selectedChordColorIndex == index
                        val composeColor = Color(pair.second)
                        Surface(
                            shape = CircleShape,
                            color = composeColor,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedChordColorIndex = index }
                                .testTag("pdf_color_$index")
                        ) {}
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = chordColors[selectedChordColorIndex].first,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chord Diagrams Selection (In the Side of the Page)
                Text(
                    text = "Side Page Chord Diagrams",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Pair(InstrumentType.PIANO, "🎹 Piano (Side)"),
                            Pair(InstrumentType.GUITAR, "🎸 Guitar (Side)")
                        ).forEach { (inst, label) ->
                            val isSelected = selectedChordDiagramInstrument == inst
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedChordDiagramInstrument = inst },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pdf_diagram_${inst.name.lowercase()}")
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            Pair(InstrumentType.BOTH, "🎹+🎸 Both (Side)"),
                            Pair(InstrumentType.NONE, "✕ None")
                        ).forEach { (inst, label) ->
                            val isSelected = selectedChordDiagramInstrument == inst
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedChordDiagramInstrument = inst },
                                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("pdf_diagram_${inst.name.lowercase()}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Header & Footer Toggles
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Include Song Header & Badges",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = includeMetadata,
                        onCheckedChange = { includeMetadata = it },
                        modifier = Modifier.testTag("pdf_toggle_header")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Include Page Numbers & Footer",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Switch(
                        checked = includeFooter,
                        onCheckedChange = { includeFooter = it },
                        modifier = Modifier.testTag("pdf_toggle_footer")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                if (isExporting) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Preparing PDF...", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Print Button
                        Button(
                            onClick = { onPrint(buildOptions()) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("pdf_print_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print", fontWeight = FontWeight.Bold)
                        }

                        // Share / Save Button
                        OutlinedButton(
                            onClick = { onShare(buildOptions()) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("pdf_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Save", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    )
}
