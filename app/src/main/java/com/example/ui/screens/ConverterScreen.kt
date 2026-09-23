package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.parser.TextToChordProConverter
import com.example.ui.i18n.AppStrings
import kotlinx.coroutines.launch

enum class SaveLocationChoice {
    DEVICE_STORAGE,
    APP_LIBRARY,
    BOTH
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    langCode: String,
    onBack: () -> Unit,
    onSaveToLibrary: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var songTitle by remember { mutableStateOf("") }

    // Save Dialog State
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(SaveLocationChoice.APP_LIBRARY) }

    // Helper to ensure ChordPro contains the user-chosen title
    fun ensureChordProTitle(chordPro: String, title: String): String {
        val cleanTitle = title.trim().ifBlank { "Converted Song" }
        val titleRegex = Regex("""^(\s*\{\s*(?:title|t)\s*:\s*)(.*?)\s*\}\s*$""", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
        return if (titleRegex.containsMatchIn(chordPro)) {
            titleRegex.replace(chordPro) { matchResult ->
                "${matchResult.groupValues[1]}$cleanTitle}"
            }
        } else {
            "{title: $cleanTitle}\n$chordPro"
        }
    }

    // File creation launcher via Storage Access Framework
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        val finalName = saveNameInput.ifBlank { songTitle.ifBlank { "Converted Song" } }
        val contentToSave = ensureChordProTitle(outputText, finalName)
        outputText = contentToSave

        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(contentToSave.toByteArray(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("File export notice: ${e.localizedMessage}")
                }
            }
        }
        // Always save to library so it displays in main page list!
        onSaveToLibrary(contentToSave, finalName)
    }

    val sampleText = """Title: Amazing Grace
Key: G

Verse 1:
G                 C                 G
Amazing grace how sweet the sound
     Em        D
That saved a wretch like me
G                 C             G
I once was lost, but now am found
      D        G
Was blind, but now I see"""

    // Function to trigger save flow
    val startSaveFlow = {
        val detected = Regex("""\{title:\s*([^}]+)\}""", RegexOption.IGNORE_CASE)
            .find(outputText)?.groupValues?.get(1)?.trim()
        saveNameInput = detected ?: songTitle.ifBlank { "My Song" }
        showSaveDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.get("text_to_chordpro", langCode),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("converter_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            inputText = sampleText
                            songTitle = "Amazing Grace"
                        },
                        modifier = Modifier.testTag("load_sample_button")
                    ) {
                        Text("Sample", fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Optional Song Title Field
            OutlinedTextField(
                value = songTitle,
                onValueChange = { songTitle = it },
                label = { Text("Song Title (optional)") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("converter_song_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. SOURCE TEXT INPUT SECTION
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("input_text_label", langCode),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Paste Button
                            OutlinedButton(
                                onClick = {
                                    val clipText = clipboardManager.getText()?.text
                                    if (!clipText.isNullOrBlank()) {
                                        inputText = clipText
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Pasted from clipboard")
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("converter_paste_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(AppStrings.get("paste", langCode), fontSize = 12.sp)
                            }

                            // Clear Button
                            if (inputText.isNotEmpty()) {
                                IconButton(
                                    onClick = { inputText = "" },
                                    modifier = Modifier.size(36.dp).testTag("converter_clear_input_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = AppStrings.get("input_text_hint", langCode),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        minLines = 8,
                        maxLines = 14,
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("converter_input_textfield")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. CONVERTED OUTPUT SECTION (Visible once converted or if text is present)
            AnimatedVisibility(visible = outputText.isNotBlank()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DataObject,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = AppStrings.get("output_text_label", langCode),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Copy Button
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(outputText))
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(AppStrings.get("copied", langCode))
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("converter_copy_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(AppStrings.get("copy", langCode), fontSize = 12.sp)
                                }

                                // Save As Button
                                Button(
                                    onClick = { startSaveFlow() },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("converter_save_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save As…", fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Output text is fully editable by the user
                        OutlinedTextField(
                            value = outputText,
                            onValueChange = { outputText = it },
                            minLines = 8,
                            maxLines = 14,
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("converter_output_textfield")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3. CONVERT BUTTON AT THE LAST (Prominently placed at the bottom, per user requirement)
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        val converted = TextToChordProConverter.convert(inputText)
                        outputText = converted

                        // Automatically trigger the prompt asking for location and name
                        val detected = Regex("""\{title:\s*([^}]+)\}""", RegexOption.IGNORE_CASE)
                            .find(converted)?.groupValues?.get(1)?.trim()
                        saveNameInput = detected ?: songTitle.ifBlank { "My Converted Song" }
                        showSaveDialog = true

                        coroutineScope.launch {
                            scrollState.animateScrollTo(scrollState.maxValue)
                        }
                    }
                },
                enabled = inputText.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("converter_convert_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoFixHigh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.get("convert_to_chordpro", langCode),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // SAVE LOCATION & NAME DIALOG (Appears automatically after conversion, and on Save button)
        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                icon = {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                },
                title = {
                    Text(
                        text = AppStrings.get("save_dialog_title", langCode),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 1. Song / File Name Input
                        Text(
                            text = AppStrings.get("save_name_label", langCode),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = saveNameInput,
                            onValueChange = { saveNameInput = it },
                            singleLine = true,
                            placeholder = { Text("e.g. Amazing Grace") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_dialog_name_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 2. Location Selection Options
                        Text(
                            text = AppStrings.get("save_location_label", langCode),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Option A: App Library (My Songs - Displays in Main Page List)
                        SaveLocationOptionCard(
                            selected = selectedLocation == SaveLocationChoice.APP_LIBRARY,
                            onClick = { selectedLocation = SaveLocationChoice.APP_LIBRARY },
                            title = "My Songs Library (Main Page List)",
                            description = "Saves as \"${saveNameInput.ifBlank { "Song" }}\" and displays immediately in the main page list",
                            icon = Icons.Default.LibraryMusic,
                            testTag = "save_option_library"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Option B: Device Storage & Main Page List
                        SaveLocationOptionCard(
                            selected = selectedLocation == SaveLocationChoice.BOTH,
                            onClick = { selectedLocation = SaveLocationChoice.BOTH },
                            title = "Device Storage (.cho) & Main Page List",
                            description = "Pick folder to save file and also display in the main page list",
                            icon = Icons.Default.Save,
                            testTag = "save_option_both"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Option C: Device Storage File
                        SaveLocationOptionCard(
                            selected = selectedLocation == SaveLocationChoice.DEVICE_STORAGE,
                            onClick = { selectedLocation = SaveLocationChoice.DEVICE_STORAGE },
                            title = "Device Storage (.cho file)",
                            description = "Save .cho file to your device and add to main page list",
                            icon = Icons.Default.Folder,
                            testTag = "save_option_storage"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSaveDialog = false
                            val finalName = saveNameInput.ifBlank { songTitle.ifBlank { "Converted Song" } }
                            val cleanBase = finalName.replace(Regex("[^a-zA-Z0-9._ -]"), "_").trim()
                            val fileName = if (cleanBase.endsWith(".cho", ignoreCase = true) ||
                                cleanBase.endsWith(".pro", ignoreCase = true) ||
                                cleanBase.endsWith(".txt", ignoreCase = true)
                            ) {
                                cleanBase
                            } else {
                                "$cleanBase.cho"
                            }

                            val contentToSave = ensureChordProTitle(outputText, finalName)
                            outputText = contentToSave

                            when (selectedLocation) {
                                SaveLocationChoice.APP_LIBRARY -> {
                                    onSaveToLibrary(contentToSave, finalName)
                                }
                                SaveLocationChoice.DEVICE_STORAGE,
                                SaveLocationChoice.BOTH -> {
                                    createDocumentLauncher.launch(fileName)
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_dialog_confirm_button")
                    ) {
                        Text("Save & Display in List", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showSaveDialog = false },
                        modifier = Modifier.testTag("save_dialog_cancel_button")
                    ) {
                        Text("Not Now")
                    }
                }
            )
        }
    }
}

@Composable
private fun SaveLocationOptionCard(
    selected: Boolean,
    onClick: () -> Unit,
    title: String,
    description: String,
    icon: ImageVector,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            }
        ),
        border = if (selected) {
            BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        },
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
