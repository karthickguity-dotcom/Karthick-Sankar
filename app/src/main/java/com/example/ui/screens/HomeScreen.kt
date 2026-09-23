package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.SongEntity
import com.example.data.parser.AccidentalMode
import com.example.data.parser.ChordProParser
import com.example.ui.components.DeleteConfirmDialog
import com.example.ui.components.PasteChordProDialog
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.RenameSongDialog
import com.example.ui.components.SongCard
import com.example.ui.components.TempoTimeSigDialog
import com.example.ui.i18n.AppStrings
import com.example.util.pdf.PdfExportOptions
import com.example.util.pdf.PdfSongExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.ui.viewmodel.SongLibraryViewModel
import com.example.ui.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SongLibraryViewModel,
    langCode: String,
    onSongSelected: (Long) -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateConverter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterFavoritesOnly by viewModel.filterFavoritesOnly.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var songToRename by remember { mutableStateOf<SongEntity?>(null) }
    var songToDelete by remember { mutableStateOf<SongEntity?>(null) }
    var songToExportPdf by remember { mutableStateOf<SongEntity?>(null) }
    var songToEditTempoTime by remember { mutableStateOf<SongEntity?>(null) }
    var isHomeExportingPdf by remember { mutableStateOf(false) }
    var showPasteDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCreatorDialog by remember { mutableStateOf(false) }

    // File picker launcher for .cho, .chordpro, .pro, .txt
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.importFromUri(uri)
        }
    }

    // Collect user notifications
    LaunchedEffect(viewModel) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .size(44.dp)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                    .clip(CircleShape)
                                    .clickable { showCreatorDialog = true }
                                    .testTag("home_logo_button")
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo_icon_1790130977112),
                                    contentDescription = "App Logo - Created by Karthick David",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.get("app_title", langCode),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = AppStrings.get("my_songs", langCode),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateSettings,
                            modifier = Modifier.testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Import & Conversion Action Buttons Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                filePickerLauncher.launch(
                                    arrayOf(
                                        "*/*",
                                        "text/*",
                                        "text/plain"
                                    )
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp)
                                .testTag("import_chordpro_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("import_chordpro", langCode),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateConverter,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(48.dp)
                                .testTag("home_converter_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("text_to_chordpro", langCode),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = { showPasteDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("paste_text_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste,
                                contentDescription = "Paste",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Search Input
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = {
                            Text(
                                text = AppStrings.get("search_songs", langCode),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_songs_field")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter Chips & Sort
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = !filterFavoritesOnly,
                                onClick = { viewModel.setFilterFavorites(false) },
                                label = { Text(AppStrings.get("all", langCode)) },
                                colors = FilterChipDefaults.filterChipColors(),
                                modifier = Modifier.testTag("filter_all_chip")
                            )

                            FilterChip(
                                selected = filterFavoritesOnly,
                                onClick = { viewModel.setFilterFavorites(true) },
                                label = { Text(AppStrings.get("favorites", langCode)) },
                                modifier = Modifier.testTag("filter_favorites_chip")
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier.testTag("sort_menu_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort Songs",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Favorites First") },
                                    onClick = {
                                        viewModel.setSortOrder(SortOrder.FAVORITES_FIRST)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Title (A to Z)") },
                                    onClick = {
                                        viewModel.setSortOrder(SortOrder.TITLE_ASC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Title (Z to A)") },
                                    onClick = {
                                        viewModel.setSortOrder(SortOrder.TITLE_DESC)
                                        showSortMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Recently Added") },
                                    onClick = {
                                        viewModel.setSortOrder(SortOrder.RECENT)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Song List
            if (songs.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = AppStrings.get("empty_library", langCode),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.get("empty_library_hint", langCode),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("songs_list")
                ) {
                    items(
                        items = songs,
                        key = { it.id }
                    ) { song ->
                        SongCard(
                            song = song,
                            langCode = langCode,
                            onClick = { onSongSelected(song.id) },
                            onFavoriteToggle = { viewModel.toggleFavorite(song.id) },
                            onRenameClick = { songToRename = song },
                            onDeleteClick = { songToDelete = song },
                            onExportPdfClick = { songToExportPdf = song },
                            onEditTempoTimeClick = { songToEditTempoTime = song }
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    songToRename?.let { song ->
        RenameSongDialog(
            initialTitle = song.title,
            langCode = langCode,
            onConfirm = { newTitle ->
                viewModel.renameSong(song.id, newTitle)
                songToRename = null
            },
            onDismiss = { songToRename = null }
        )
    }

    songToDelete?.let { song ->
        DeleteConfirmDialog(
            songTitle = song.title,
            langCode = langCode,
            onConfirm = {
                viewModel.deleteSong(song.id)
                songToDelete = null
            },
            onDismiss = { songToDelete = null }
        )
    }

    songToEditTempoTime?.let { song ->
        TempoTimeSigDialog(
            songTitle = song.title,
            initialTempo = song.displayTempo,
            initialTimeSignature = song.displayTimeSignature,
            langCode = langCode,
            onConfirm = { tempo, timeSig ->
                viewModel.updateTempoAndTimeSignature(song.id, tempo, timeSig)
                songToEditTempoTime = null
            },
            onDismiss = { songToEditTempoTime = null }
        )
    }

    if (showPasteDialog) {
        PasteChordProDialog(
            langCode = langCode,
            onImportText = { text, title ->
                viewModel.importFromText(text, title)
                showPasteDialog = false
            },
            onDismiss = { showPasteDialog = false }
        )
    }

    if (showCreatorDialog) {
        AlertDialog(
            onDismissRequest = { showCreatorDialog = false },
            confirmButton = {
                TextButton(
                    onClick = { showCreatorDialog = false },
                    modifier = Modifier.testTag("creator_dialog_close")
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .size(38.dp)
                            .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo_icon_1790130977112),
                            contentDescription = "App Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "App Creator",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Karthick David",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("home_creator_name")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Designed & crafted for musicians and live worship performances",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ChordPro Stage Pro • v1.0.0",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }
            }
        )
    }

    songToExportPdf?.let { song ->
        val mode = AccidentalMode.SHARP // default or active mode
        val parsed = remember(song.rawChordPro) {
            ChordProParser.parse(song.rawChordPro, song.title)
        }
        PdfExportDialog(
            songTitle = song.title,
            currentKey = song.originalKey.ifBlank { parsed.key },
            transposeSemitones = 0,
            accidentalMode = mode,
            isExporting = isHomeExportingPdf,
            onPrint = { options ->
                coroutineScope.launch(Dispatchers.IO) {
                    isHomeExportingPdf = true
                    try {
                        val file = PdfSongExporter.generatePdf(
                            context = context,
                            parsedSong = parsed,
                            songTitle = song.title,
                            artist = song.artist,
                            originalKey = song.originalKey,
                            capo = song.capo,
                            transposeSemitones = 0,
                            accidentalMode = mode,
                            options = options
                        )
                        withContext(Dispatchers.Main) {
                            PdfSongExporter.printDocument(context, file, song.title)
                            songToExportPdf = null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isHomeExportingPdf = false
                    }
                }
            },
            onShare = { options ->
                coroutineScope.launch(Dispatchers.IO) {
                    isHomeExportingPdf = true
                    try {
                        val file = PdfSongExporter.generatePdf(
                            context = context,
                            parsedSong = parsed,
                            songTitle = song.title,
                            artist = song.artist,
                            originalKey = song.originalKey,
                            capo = song.capo,
                            transposeSemitones = 0,
                            accidentalMode = mode,
                            options = options
                        )
                        withContext(Dispatchers.Main) {
                            PdfSongExporter.sharePdf(context, file, song.title)
                            songToExportPdf = null
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        isHomeExportingPdf = false
                    }
                }
            },
            onDismiss = { songToExportPdf = null }
        )
    }
}
