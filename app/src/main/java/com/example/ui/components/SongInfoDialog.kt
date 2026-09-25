package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Copyright
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.chordpro.SongInfoData

@Composable
fun SongInfoDialog(
    initialInfo: SongInfoData,
    langCode: String,
    onConfirm: (SongInfoData) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(initialInfo.title) }
    var artist by remember { mutableStateOf(initialInfo.artist) }
    var album by remember { mutableStateOf(initialInfo.album) }
    var key by remember { mutableStateOf(initialInfo.key) }
    var tempo by remember { mutableStateOf(initialInfo.tempo) }
    var timeSignature by remember { mutableStateOf(initialInfo.timeSignature) }
    var capo by remember { mutableIntStateOf(initialInfo.capo) }
    var copyright by remember { mutableStateOf(initialInfo.copyright) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Column {
                    Text(
                        text = "Song Information",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "ChordPro Metadata & Directives",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
                    .verticalScroll(scrollState)
                    .testTag("song_info_dialog"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_info_title_input")
                )

                // Artist
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist / Composer") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_info_artist_input")
                )

                // Album
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    leadingIcon = { Icon(Icons.Default.Album, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_info_album_input")
                )

                // Key
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it.take(8) },
                    label = { Text("Original Key (e.g. C, G, Em)") },
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_info_key_input")
                )

                // Quick Key Selection Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("C", "D", "E", "G", "A", "Am", "Em").forEach { k ->
                        FilterChip(
                            selected = key.equals(k, ignoreCase = true),
                            onClick = { key = k },
                            label = { Text(k, fontSize = 11.sp) },
                            modifier = Modifier.testTag("key_chip_$k")
                        )
                    }
                }

                // Tempo & Time Signature Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = tempo,
                        onValueChange = { tempo = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text("Tempo (BPM)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("song_info_tempo_input")
                    )

                    OutlinedTextField(
                        value = timeSignature,
                        onValueChange = { timeSignature = it.take(6) },
                        label = { Text("Time (e.g. 4/4)") },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("song_info_time_input")
                    )
                }

                // Capo Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Capo: ${if (capo == 0) "None" else "Fret $capo"}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (0..5).forEach { fret ->
                            FilterChip(
                                selected = capo == fret,
                                onClick = { capo = fret },
                                label = { Text(if (fret == 0) "0" else "$fret", fontSize = 11.sp) },
                                modifier = Modifier.testTag("capo_chip_$fret")
                            )
                        }
                    }
                }

                // Copyright
                OutlinedTextField(
                    value = copyright,
                    onValueChange = { copyright = it },
                    label = { Text("Copyright Information") },
                    placeholder = { Text("e.g. © 2024 Hillsong Music") },
                    leadingIcon = { Icon(Icons.Default.Copyright, contentDescription = null) },
                    singleLine = false,
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("song_info_copyright_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        SongInfoData(
                            title = title.trim(),
                            artist = artist.trim(),
                            album = album.trim(),
                            key = key.trim(),
                            copyright = copyright.trim(),
                            tempo = tempo.trim(),
                            timeSignature = timeSignature.trim(),
                            capo = capo
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("song_info_save_button")
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("song_info_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
