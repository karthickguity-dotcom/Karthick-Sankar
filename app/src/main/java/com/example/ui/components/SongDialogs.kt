package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.i18n.AppStrings

@Composable
fun RenameSongDialog(
    initialTitle: String,
    langCode: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var titleText by remember { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppStrings.get("rename", langCode),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = titleText,
                    onValueChange = { titleText = it },
                    label = { Text("Song Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_input_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (titleText.isNotBlank()) {
                        onConfirm(titleText.trim())
                    }
                },
                enabled = titleText.isNotBlank(),
                modifier = Modifier.testTag("rename_save_button")
            ) {
                Text(AppStrings.get("save", langCode))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("rename_cancel_button")
            ) {
                Text(AppStrings.get("cancel", langCode))
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    songTitle: String,
    langCode: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppStrings.get("delete_confirm_title", langCode),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "${AppStrings.get("delete_confirm_desc", langCode)}\n\n\"$songTitle\"",
                    fontSize = 14.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.testTag("delete_confirm_button")
            ) {
                Text(AppStrings.get("delete", langCode))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("delete_cancel_button")
            ) {
                Text(AppStrings.get("cancel", langCode))
            }
        }
    )
}

@Composable
fun PasteChordProDialog(
    langCode: String,
    onImportText: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var rawText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppStrings.get("paste_chordpro", langCode),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Song Title (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("ChordPro Lyrics & Chords") },
                    placeholder = { Text("{title: Song}\n{key: G}\n[G]Amazing [C]grace...") },
                    minLines = 8,
                    maxLines = 14,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paste_chordpro_text_field")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        onImportText(
                            rawText.trim(),
                            title.ifBlank { "Pasted Song" }
                        )
                    }
                },
                enabled = rawText.isNotBlank(),
                modifier = Modifier.testTag("paste_import_button")
            ) {
                Text(AppStrings.get("save", langCode))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.get("cancel", langCode))
            }
        }
    )
}

@Composable
fun TempoTimeSigDialog(
    songTitle: String,
    initialTempo: String,
    initialTimeSignature: String,
    langCode: String,
    onConfirm: (tempo: String, timeSignature: String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempoText by remember { mutableStateOf(initialTempo) }
    var timeSigText by remember { mutableStateOf(initialTimeSignature.ifBlank { "4/4" }) }

    // Tap tempo calculation helper
    var tapTimestamps by remember { mutableStateOf(listOf<Long>()) }

    fun registerTap() {
        val now = System.currentTimeMillis()
        // If last tap was > 2.5s ago, reset tap history
        val recentTaps = if (tapTimestamps.isNotEmpty() && now - tapTimestamps.last() > 2500) {
            listOf(now)
        } else {
            (tapTimestamps + now).takeLast(6)
        }
        tapTimestamps = recentTaps

        if (recentTaps.size >= 2) {
            val intervals = recentTaps.zipWithNext { a, b -> b - a }
            val avgMs = intervals.average()
            if (avgMs > 0) {
                val bpm = (60000.0 / avgMs).toInt().coerceIn(30, 300)
                tempoText = bpm.toString()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "Tempo & Time Signature",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = songTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Tempo Section
                Column {
                    Text(
                        text = "Tempo (BPM)",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = tempoText,
                            onValueChange = { input ->
                                tempoText = input.filter { it.isDigit() }.take(3)
                            },
                            label = { Text("BPM (e.g. 72)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tempo_input_field")
                        )

                        // Decrement button
                        IconButton(
                            onClick = {
                                val current = tempoText.toIntOrNull() ?: 70
                                tempoText = (current - 2).coerceAtLeast(30).toString()
                            },
                            modifier = Modifier.testTag("tempo_minus_button")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease Tempo")
                        }

                        // Increment button
                        IconButton(
                            onClick = {
                                val current = tempoText.toIntOrNull() ?: 70
                                tempoText = (current + 2).coerceAtMost(300).toString()
                            },
                            modifier = Modifier.testTag("tempo_plus_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase Tempo")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Preset chips & Tap Tempo
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("60", "72", "80", "120").forEach { preset ->
                            FilterChip(
                                selected = tempoText == preset,
                                onClick = { tempoText = preset },
                                label = { Text(preset, fontSize = 11.sp) },
                                modifier = Modifier.testTag("tempo_chip_$preset")
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { registerTap() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("tap_tempo_button")
                        ) {
                            Icon(
                                Icons.Default.TouchApp,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Tap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // 2. Time Signature Section
                Column {
                    Text(
                        text = "Time Signature",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = timeSigText,
                        onValueChange = { timeSigText = it.take(6) },
                        label = { Text("Signature (e.g. 4/4, 3/4)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("time_sig_input_field")
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("4/4", "3/4", "6/8", "2/4", "12/8").forEach { sig ->
                            FilterChip(
                                selected = timeSigText == sig,
                                onClick = { timeSigText = sig },
                                label = { Text(sig, fontSize = 11.sp) },
                                modifier = Modifier.testTag("time_sig_chip_${sig.replace('/', '_')}")
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(tempoText.trim(), timeSigText.trim())
                },
                modifier = Modifier.testTag("tempo_time_save_button")
            ) {
                Text(AppStrings.get("save", langCode))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("tempo_time_cancel_button")
            ) {
                Text(AppStrings.get("cancel", langCode))
            }
        }
    )
}
