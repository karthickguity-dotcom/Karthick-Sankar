package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.parser.AccidentalMode
import com.example.data.parser.Transposer
import com.example.data.preferences.AppDisplaySettings
import com.example.data.preferences.AppSettingsManager
import com.example.ui.components.ChordDetailDialog
import com.example.ui.components.ChordDiagramStrip
import com.example.ui.components.ChordProSongView
import com.example.ui.components.LyricsTransliterationDialog
import com.example.ui.components.PdfExportDialog
import com.example.ui.components.PracticeSessionPanel
import com.example.ui.components.SongInfoDialog
import com.example.ui.components.TempoTimeSigDialog
import com.example.ui.components.TunerDialog
import com.example.ui.i18n.AppStrings
import com.example.ui.viewmodel.LyricsViewModel
import com.example.util.chord.InstrumentType
import com.example.util.chordpro.SongInfoData
import com.example.util.pdf.PdfExportOptions
import com.example.util.pdf.PdfSongExporter
import com.example.util.transliteration.TransliterationTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LyricsScreen(
    songId: Long,
    viewModel: LyricsViewModel,
    settings: AppDisplaySettings,
    langCode: String,
    onBack: () -> Unit,
    onNavigateSettings: () -> Unit,
    onUpdateAccidentalMode: ((String) -> Unit)? = null,
    onUpdatePreferredInstrument: ((String) -> Unit)? = null,
    onNavigateTuner: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val currentSong by viewModel.currentSong.collectAsStateWithLifecycle()
    val parsedSong by viewModel.parsedSong.collectAsStateWithLifecycle()
    val transposeSemitones by viewModel.transposeSemitones.collectAsStateWithLifecycle()
    val accidentalMode by viewModel.accidentalMode.collectAsStateWithLifecycle()
    val isAutoScrolling by viewModel.isAutoScrolling.collectAsStateWithLifecycle()
    val autoScrollSpeed by viewModel.autoScrollSpeed.collectAsStateWithLifecycle()
    val isFullScreen by viewModel.isFullScreen.collectAsStateWithLifecycle()
    val showOnlyLyrics by viewModel.showOnlyLyrics.collectAsStateWithLifecycle()
    val transliterationTarget by viewModel.transliterationTarget.collectAsStateWithLifecycle()
    val practiceState by viewModel.practiceState.collectAsStateWithLifecycle()

    var showTransliterationDialog by remember { mutableStateOf(false) }
    var showSongInfoDialog by remember { mutableStateOf(false) }
    var showPracticeSession by remember { mutableStateOf(false) }
    var showTunerDialog by remember { mutableStateOf(false) }

    var selectedInstrument by remember {
        mutableStateOf(InstrumentType.NONE)
    }
    var clickedChordForDetail by remember { mutableStateOf<String?>(null) }

    val uniqueChords = remember(parsedSong, transposeSemitones, accidentalMode) {
        val song = parsedSong ?: return@remember emptyList()
        val list = mutableListOf<String>()
        song.sections.forEach { section ->
            section.lines.forEach { line ->
                line.pairs.forEach { pair ->
                    pair.chord?.takeIf { it.isNotBlank() }?.let { raw ->
                        val transposed = Transposer.transposeChord(raw, transposeSemitones, accidentalMode)
                        if (!list.contains(transposed)) {
                            list.add(transposed)
                        }
                    }
                }
            }
        }
        list
    }

    var showAutoScrollControls by remember { mutableStateOf(false) }
    var showPdfExportDialog by remember { mutableStateOf(false) }
    var showTempoTimeDialog by remember { mutableStateOf(false) }
    var isExportingPdf by remember { mutableStateOf(false) }

    val handlePrintPdf: (PdfExportOptions) -> Unit = { options ->
        val song = parsedSong
        if (song != null) {
            coroutineScope.launch(Dispatchers.IO) {
                isExportingPdf = true
                try {
                    val pdfFile = PdfSongExporter.generatePdf(
                        context = context,
                        parsedSong = song,
                        songTitle = currentSong?.title ?: song.title,
                        artist = currentSong?.artist ?: song.artist,
                        originalKey = currentSong?.originalKey ?: song.key,
                        capo = currentSong?.capo ?: song.capo,
                        transposeSemitones = transposeSemitones,
                        accidentalMode = accidentalMode,
                        options = options
                    )
                    withContext(Dispatchers.Main) {
                        PdfSongExporter.printDocument(
                            context = context,
                            pdfFile = pdfFile,
                            jobName = currentSong?.title ?: song.title
                        )
                        showPdfExportDialog = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isExportingPdf = false
                }
            }
        }
    }

    val handleSharePdf: (PdfExportOptions) -> Unit = { options ->
        val song = parsedSong
        if (song != null) {
            coroutineScope.launch(Dispatchers.IO) {
                isExportingPdf = true
                try {
                    val pdfFile = PdfSongExporter.generatePdf(
                        context = context,
                        parsedSong = song,
                        songTitle = currentSong?.title ?: song.title,
                        artist = currentSong?.artist ?: song.artist,
                        originalKey = currentSong?.originalKey ?: song.key,
                        capo = currentSong?.capo ?: song.capo,
                        transposeSemitones = transposeSemitones,
                        accidentalMode = accidentalMode,
                        options = options
                    )
                    withContext(Dispatchers.Main) {
                        PdfSongExporter.sharePdf(
                            context = context,
                            pdfFile = pdfFile,
                            songTitle = currentSong?.title ?: song.title
                        )
                        showPdfExportDialog = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    isExportingPdf = false
                }
            }
        }
    }

    // Load song when screen opens (keyed on songId only so accidental mode changes do not reload/reset)
    LaunchedEffect(songId) {
        viewModel.loadSong(songId, AccidentalMode.fromString(settings.accidentalMode))
    }

    val switchAccidentalMode: (AccidentalMode) -> Unit = { newMode ->
        viewModel.setAccidentalMode(newMode)
        onUpdateAccidentalMode?.invoke(newMode.name)
    }

    // Keep screen awake during lyrics view if enabled
    DisposableEffect(settings.keepScreenOn) {
        val window = (context as? Activity)?.window
        if (settings.keepScreenOn) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Stop metronome when leaving the lyrics screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPracticeSession()
        }
    }

    // Smooth Standard Auto-Scrolling Loop
    LaunchedEffect(isAutoScrolling, autoScrollSpeed) {
        if (isAutoScrolling) {
            while (isActive && isAutoScrolling) {
                // Scroll small amount every 50ms for buttery smoothness
                val stepPixels = (autoScrollSpeed * 1.5f)
                scrollState.scrollBy(stepPixels)
                delay(40)
            }
        }
    }

    // Metronome-Relative Auto-Scrolling Loop for Practice Session
    // Scrolls the lyrics and chords at a pace directly synchronized with the metronome timing (BPM & pace multiplier)
    LaunchedEffect(
        practiceState.isPlaying,
        practiceState.isCountingIn,
        practiceState.relativeScrollEnabled,
        practiceState.bpm,
        practiceState.scrollPaceMultiplier
    ) {
        if (practiceState.isPlaying && !practiceState.isCountingIn && practiceState.relativeScrollEnabled) {
            val frameIntervalMs = 25L
            while (isActive && practiceState.isPlaying && !practiceState.isCountingIn && practiceState.relativeScrollEnabled) {
                val pixelsPerSec = practiceState.getScrollPixelsPerSecond()
                val delta = pixelsPerSec * (frameIntervalMs / 1000f)
                if (delta > 0f) {
                    scrollState.scrollBy(delta)
                }
                delay(frameIntervalMs)
            }
        }
    }

    // Colors derived from user's theme selection
    val activeTheme = AppSettingsManager.THEMES.getOrElse(settings.themeIndex) { AppSettingsManager.THEMES[0] }
    val chosenChordColor = AppSettingsManager.CHORD_COLORS.getOrElse(settings.chordColorIndex) {
        Pair("", activeTheme.defaultChordColor)
    }.second

    val backgroundColor = activeTheme.backgroundColor
    val surfaceColor = activeTheme.surfaceColor
    val textColor = activeTheme.textColor
    val secondaryColor = activeTheme.secondaryTextColor

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = backgroundColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. TOP HEADER (Hidden if Fullscreen Mode is active)
            AnimatedVisibility(visible = !isFullScreen) {
                Surface(
                    color = surfaceColor,
                    tonalElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            // Back Button
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("lyrics_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = textColor
                                )
                            }

                            // Song Title & Artist
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = currentSong?.title ?: parsedSong?.title ?: "Lyrics",
                                    color = textColor,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    textAlign = TextAlign.Center
                                )
                                val artist = currentSong?.artist ?: parsedSong?.artist ?: ""
                                val album = currentSong?.album?.ifBlank { null } ?: parsedSong?.album?.ifBlank { null }
                                val subText = when {
                                    artist.isNotBlank() && album != null -> "$artist • $album"
                                    artist.isNotBlank() -> artist
                                    album != null -> album
                                    else -> ""
                                }
                                if (subText.isNotBlank()) {
                                    Text(
                                        text = subText,
                                        color = secondaryColor,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Action buttons: Practice Metronome, Tuner, Lyrics Mode toggle, Transliteration, Song Info, PDF Export, Fullscreen, Settings
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Practice Session & Metronome button
                                IconButton(
                                    onClick = { showPracticeSession = !showPracticeSession },
                                    modifier = Modifier.testTag("lyrics_practice_session_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = "Practice Session & Metronome",
                                        tint = if (practiceState.isPlaying || showPracticeSession) chosenChordColor else textColor
                                    )
                                }
                                // Instrument Tuner button
                                IconButton(
                                    onClick = { showTunerDialog = true },
                                    modifier = Modifier.testTag("lyrics_tuner_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Instrument Tuner",
                                        tint = if (showTunerDialog) chosenChordColor else textColor
                                    )
                                }
                                // Toggle Show Only Lyrics
                                IconButton(
                                    onClick = { viewModel.toggleShowOnlyLyrics() },
                                    modifier = Modifier.testTag("lyrics_toggle_lyrics_only_button")
                                ) {
                                    Icon(
                                        imageVector = if (showOnlyLyrics) Icons.Default.MusicNote else Icons.Default.TextFields,
                                        contentDescription = if (showOnlyLyrics) "Show Chords & Lyrics" else "Show Only Lyrics",
                                        tint = if (showOnlyLyrics) chosenChordColor else textColor
                                    )
                                }
                                // Transliteration button
                                IconButton(
                                    onClick = { showTransliterationDialog = true },
                                    modifier = Modifier.testTag("lyrics_transliteration_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Transliterate Lyrics",
                                        tint = if (transliterationTarget != TransliterationTarget.ORIGINAL) chosenChordColor else textColor
                                    )
                                }
                                // Song Info button
                                IconButton(
                                    onClick = { showSongInfoDialog = true },
                                    modifier = Modifier.testTag("lyrics_song_info_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Song Info",
                                        tint = textColor
                                    )
                                }
                                IconButton(
                                    onClick = { showPdfExportDialog = true },
                                    modifier = Modifier.testTag("lyrics_export_pdf_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Print,
                                        contentDescription = "Export or Print PDF",
                                        tint = textColor
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleFullScreen() },
                                    modifier = Modifier.testTag("lyrics_fullscreen_button")
                                ) {
                                    Icon(
                                        imageVector = if (isFullScreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                        contentDescription = "Toggle Fullscreen",
                                        tint = textColor
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        if (onNavigateTuner != null) onNavigateTuner() else showTunerDialog = true
                                    },
                                    modifier = Modifier.testTag("lyrics_tuner_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Instrument Tuner",
                                        tint = textColor
                                    )
                                }
                                IconButton(
                                    onClick = onNavigateSettings,
                                    modifier = Modifier.testTag("lyrics_settings_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings",
                                        tint = textColor
                                    )
                                }
                            }
                        }

                        // Sub-header: Key, Tempo, Time Signature & Capo information
                        val currentKey = remember(currentSong, parsedSong, transposeSemitones, accidentalMode) {
                            viewModel.getCurrentKey()
                        }
                        val originalKey = currentSong?.originalKey ?: parsedSong?.key ?: ""
                        val capo = currentSong?.capo ?: parsedSong?.capo ?: 0
                        val songTempo = currentSong?.displayTempo?.ifBlank { null } ?: parsedSong?.tempo?.ifBlank { null } ?: ""
                        val songTimeSig = currentSong?.displayTimeSignature?.ifBlank { null } ?: parsedSong?.timeSignature?.ifBlank { null } ?: ""

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            if (currentKey.isNotBlank()) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "${AppStrings.get("key", langCode)}: $currentKey",
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        if (transposeSemitones != 0) {
                                            val sign = if (transposeSemitones > 0) "+$transposeSemitones" else "$transposeSemitones"
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "($sign)",
                                                color = chosenChordColor,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // Tempo Badge
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = { showTempoTimeDialog = true },
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("lyrics_tempo_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Tempo",
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (songTempo.isNotBlank()) "$songTempo BPM" else "BPM",
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Time Signature Badge
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = { showTempoTimeDialog = true },
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("lyrics_time_sig_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Time Signature",
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (songTimeSig.isNotBlank()) songTimeSig else "4/4",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            if (capo > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${AppStrings.get("capo", langCode)}: $capo",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Sharp / Flat Mode Toggle Selector
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("accidental_mode_selector")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    // Sharp (♯)
                                    Surface(
                                        onClick = { switchAccidentalMode(AccidentalMode.SHARP) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (accidentalMode == AccidentalMode.SHARP) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.testTag("accidental_mode_sharp")
                                    ) {
                                        Text(
                                            text = "♯ Sharp",
                                            fontSize = 12.sp,
                                            fontWeight = if (accidentalMode == AccidentalMode.SHARP) FontWeight.Bold else FontWeight.Medium,
                                            color = if (accidentalMode == AccidentalMode.SHARP) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    // Flat (♭)
                                    Surface(
                                        onClick = { switchAccidentalMode(AccidentalMode.FLAT) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (accidentalMode == AccidentalMode.FLAT) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.testTag("accidental_mode_flat")
                                    ) {
                                        Text(
                                            text = "♭ Flat",
                                            fontSize = 12.sp,
                                            fontWeight = if (accidentalMode == AccidentalMode.FLAT) FontWeight.Bold else FontWeight.Medium,
                                            color = if (accidentalMode == AccidentalMode.FLAT) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.75f),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Sub-header Row 2: View Mode (Chords+Lyrics vs Only Lyrics) & Transliteration Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            // Mode Switch Pill
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("lyrics_mode_toggle_selector")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Surface(
                                        onClick = { viewModel.setShowOnlyLyrics(false) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (!showOnlyLyrics) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.testTag("mode_chords_lyrics")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = null,
                                                tint = if (!showOnlyLyrics) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.75f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Chords & Lyrics",
                                                fontSize = 12.sp,
                                                fontWeight = if (!showOnlyLyrics) FontWeight.Bold else FontWeight.Medium,
                                                color = if (!showOnlyLyrics) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.75f)
                                            )
                                        }
                                    }

                                    Surface(
                                        onClick = { viewModel.setShowOnlyLyrics(true) },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (showOnlyLyrics) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.testTag("mode_only_lyrics")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.TextFields,
                                                contentDescription = null,
                                                tint = if (showOnlyLyrics) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.75f),
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Only Lyrics",
                                                fontSize = 12.sp,
                                                fontWeight = if (showOnlyLyrics) FontWeight.Bold else FontWeight.Medium,
                                                color = if (showOnlyLyrics) MaterialTheme.colorScheme.onPrimary else textColor.copy(alpha = 0.75f)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Transliteration Quick Chip
                            Surface(
                                onClick = { showTransliterationDialog = true },
                                color = if (transliterationTarget != TransliterationTarget.ORIGINAL) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = if (transliterationTarget != TransliterationTarget.ORIGINAL) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                } else null,
                                modifier = Modifier.testTag("lyrics_transliteration_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Transliterate",
                                        tint = if (transliterationTarget != TransliterationTarget.ORIGINAL) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            textColor.copy(alpha = 0.75f)
                                        },
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (transliterationTarget != TransliterationTarget.ORIGINAL) {
                                            "Script: ${transliterationTarget.shortBadge}"
                                        } else {
                                            "Transliterate"
                                        },
                                        fontSize = 12.sp,
                                        fontWeight = if (transliterationTarget != TransliterationTarget.ORIGINAL) FontWeight.Bold else FontWeight.Medium,
                                        color = if (transliterationTarget != TransliterationTarget.ORIGINAL) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            textColor.copy(alpha = 0.75f)
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Tuner Quick Chip
                            Surface(
                                onClick = { showTunerDialog = true },
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("lyrics_tuner_quick_chip")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Tuner",
                                        tint = textColor.copy(alpha = 0.75f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tuner",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor.copy(alpha = 0.75f)
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            color = textColor.copy(alpha = 0.12f),
                            thickness = 1.dp
                        )
                    }
                }
            }

            // Chord Diagram Strip (Guitar / Piano / Hide selector with diagram carousel)
            // Hidden automatically when in "Show Only Lyrics" mode
            if (!isFullScreen && !showOnlyLyrics && uniqueChords.isNotEmpty()) {
                ChordDiagramStrip(
                    chords = uniqueChords,
                    selectedInstrument = selectedInstrument,
                    onInstrumentChanged = { newInst ->
                        selectedInstrument = newInst
                        onUpdatePreferredInstrument?.invoke(newInst.name)
                    },
                    chordColor = chosenChordColor,
                    textColor = textColor,
                    accidentalMode = accidentalMode
                )
            }

            // Minimal Fullscreen Exit Button
            if (isFullScreen) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Surface(
                        color = surfaceColor.copy(alpha = 0.7f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(36.dp)
                    ) {
                        IconButton(onClick = { viewModel.toggleFullScreen() }) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Exit Fullscreen",
                                tint = textColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 2. CENTRAL SCROLLABLE LYRICS VIEWPORT
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (parsedSong == null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .testTag("lyrics_scroll_container")
                    ) {
                        // Song Sheet Info Banner (Visible even in fullscreen mode for stage performance)
                        val songTempo = currentSong?.displayTempo?.ifBlank { null } ?: parsedSong?.tempo?.ifBlank { null } ?: ""
                        val songTimeSig = currentSong?.displayTimeSignature?.ifBlank { null } ?: parsedSong?.timeSignature?.ifBlank { null } ?: ""
                        val displayKey = remember(currentSong, parsedSong, transposeSemitones, accidentalMode) {
                            viewModel.getCurrentKey()
                        }
                        val songCapo = currentSong?.capo ?: parsedSong?.capo ?: 0

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = surfaceColor.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, textColor.copy(alpha = 0.12f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("lyrics_sheet_info_banner")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                if (displayKey.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Key: ",
                                            fontSize = 12.sp,
                                            color = secondaryColor
                                        )
                                        Text(
                                            text = displayKey,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = textColor
                                        )
                                        if (transposeSemitones != 0) {
                                            val sign = if (transposeSemitones > 0) "+$transposeSemitones" else "$transposeSemitones"
                                            Text(
                                                text = " ($sign)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = chosenChordColor
                                            )
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showTempoTimeDialog = true }
                                        .testTag("sheet_tempo_indicator")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Tempo",
                                        tint = chosenChordColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (songTempo.isNotBlank()) "♩ $songTempo BPM" else "♩ Set BPM",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showTempoTimeDialog = true }
                                        .testTag("sheet_time_sig_indicator")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Time Signature",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (songTimeSig.isNotBlank()) "Time: $songTimeSig" else "Time: 4/4",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showPracticeSession = !showPracticeSession }
                                        .testTag("sheet_practice_indicator")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.HourglassTop,
                                        contentDescription = "Practice Metronome",
                                        tint = if (practiceState.isPlaying) chosenChordColor else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (practiceState.isPlaying) "Practice (${practiceState.bpm})" else "Practice",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (practiceState.isPlaying) chosenChordColor else textColor
                                    )
                                }

                                if (songCapo > 0) {
                                    Text(
                                        text = "Capo $songCapo",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = secondaryColor
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { showSongInfoDialog = true }
                                        .testTag("sheet_song_info_indicator")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Song Info",
                                        tint = chosenChordColor,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Info",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        ChordProSongView(
                            parsedSong = parsedSong!!,
                            transposeSemitones = transposeSemitones,
                            accidentalMode = accidentalMode,
                            lyricsFontSize = settings.lyricsFontSize,
                            chordFontSize = settings.chordFontSize,
                            lineSpacing = settings.lineSpacing,
                            chordColor = chosenChordColor,
                            lyricsColor = textColor,
                            showOnlyLyrics = showOnlyLyrics,
                            transliterationTarget = transliterationTarget,
                            onChordClick = { chord -> clickedChordForDetail = chord }
                        )

                        // Copyright Notice & Album Footer Display
                        val copyrightText = currentSong?.copyright?.ifBlank { null } ?: parsedSong?.copyright?.ifBlank { null }
                        val albumText = currentSong?.album?.ifBlank { null } ?: parsedSong?.album?.ifBlank { null }

                        if (copyrightText != null || albumText != null) {
                            Spacer(modifier = Modifier.height(24.dp))
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .testTag("lyrics_footer_song_info")
                            ) {
                                if (albumText != null) {
                                    Text(
                                        text = "Album: $albumText",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = secondaryColor.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                if (copyrightText != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = copyrightText,
                                        fontSize = 11.sp,
                                        color = secondaryColor.copy(alpha = 0.65f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        // Bottom padding so fixed controls don't obscure last lyrics
                        Spacer(modifier = Modifier.height(180.dp))
                    }
                }
            }

            // 3. FIXED BOTTOM CONTROLS (Always on top of lyrics, Stage-Ready)
            Surface(
                color = surfaceColor,
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fixed_bottom_controls")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Practice Session Metronome & Relative Scroll Control Panel
                    AnimatedVisibility(
                        visible = showPracticeSession,
                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                    ) {
                        PracticeSessionPanel(
                            state = practiceState,
                            chordColor = chosenChordColor,
                            onTogglePlay = { viewModel.togglePracticeSession() },
                            onResetSession = {
                                viewModel.resetPracticeSession()
                                coroutineScope.launch { scrollState.animateScrollTo(0) }
                            },
                            onAdjustBpm = { delta -> viewModel.adjustPracticeBpm(delta) },
                            onSetBpm = { bpm -> viewModel.setPracticeBpm(bpm) },
                            onTapTempo = { viewModel.registerPracticeTap() },
                            onSetTimeSignature = { timeSig -> viewModel.setPracticeTimeSignature(timeSig) },
                            onToggleMute = { viewModel.togglePracticeMute() },
                            onToggleRelativeScroll = { viewModel.togglePracticeRelativeScroll() },
                            onSetScrollPace = { pace -> viewModel.setPracticeScrollPace(pace) },
                            onSetCountInBars = { bars -> viewModel.setPracticeCountInBars(bars) },
                            onNudgeScroll = { pixels ->
                                coroutineScope.launch {
                                    val target = (scrollState.value + pixels).coerceIn(0f, scrollState.maxValue.toFloat())
                                    scrollState.animateScrollTo(target.toInt())
                                }
                            },
                            onScrollToTop = {
                                coroutineScope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            },
                            onSaveBpmToSong = { viewModel.savePracticeBpmToSong() },
                            onOpenTuner = {
                                if (onNavigateTuner != null) onNavigateTuner() else showTunerDialog = true
                            },
                            onClose = { showPracticeSession = false },
                            modifier = Modifier.padding(bottom = 10.dp)
                        )
                    }

                    // Optional Auto-Scroll Speed Popout
                    AnimatedVisibility(visible = showAutoScrollControls) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(backgroundColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${AppStrings.get("speed", langCode)}: ${autoScrollSpeed}x",
                                color = textColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { viewModel.setAutoScrollSpeed(autoScrollSpeed - 1) },
                                    enabled = autoScrollSpeed > 1,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Remove,
                                        contentDescription = "Slow Down",
                                        tint = textColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.setAutoScrollSpeed(autoScrollSpeed + 1) },
                                    enabled = autoScrollSpeed < 10,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Speed Up",
                                        tint = textColor
                                    )
                                }
                            }
                        }
                    }

                    // A. TRANSPOSE CONTROLS ROW: [ − ] TRANSPOSE [ + ] [ Reset ]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        // Transpose Decrement (−)
                        Button(
                            onClick = { viewModel.transposeDown() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .weight(1f)
                                .testTag("transpose_down_button")
                        ) {
                            Text(
                                text = "−",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Central Indicator
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(2f)
                                .padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = AppStrings.get("transpose", langCode),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = secondaryColor
                            )
                            val keyDisplay = remember(currentSong, parsedSong, transposeSemitones, accidentalMode) {
                                viewModel.getCurrentKey()
                            }
                            val offsetDisplay = when {
                                transposeSemitones > 0 -> "+$transposeSemitones"
                                transposeSemitones < 0 -> "$transposeSemitones"
                                else -> "0"
                            }
                            Text(
                                text = if (keyDisplay.isNotBlank()) "$keyDisplay ($offsetDisplay)" else offsetDisplay,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (transposeSemitones != 0) chosenChordColor else textColor
                            )
                        }

                        // Transpose Increment (+)
                        Button(
                            onClick = { viewModel.transposeUp() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier
                                .height(48.dp)
                                .weight(1f)
                                .testTag("transpose_up_button")
                        ) {
                            Text(
                                text = "+",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Accidental Mode quick toggle button (♯ / ♭)
                        Spacer(modifier = Modifier.width(6.dp))
                        FilledTonalButton(
                            onClick = {
                                val next = if (accidentalMode == AccidentalMode.SHARP) AccidentalMode.FLAT else AccidentalMode.SHARP
                                switchAccidentalMode(next)
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("lyrics_accidental_mode_toggle")
                        ) {
                            Text(
                                text = if (accidentalMode == AccidentalMode.SHARP) "♯" else "♭",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Reset Transpose button
                        if (transposeSemitones != 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            OutlinedButton(
                                onClick = { viewModel.resetTranspose() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("transpose_reset_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset",
                                    tint = chosenChordColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // B. LARGE PAGE UP / PAGE DOWN CONTROLS + AUTO-SCROLL
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // ▲ PAGE UP
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Move up by ~800 pixels (approx 1 screen)
                                    scrollState.animateScrollTo((scrollState.value - 800).coerceAtLeast(0))
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp)
                                .testTag("page_up_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.get("page_up", langCode),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Auto-scroll toggle pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isAutoScrolling) chosenChordColor else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                IconButton(
                                    onClick = { viewModel.toggleAutoScroll() },
                                    modifier = Modifier.testTag("auto_scroll_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = "Auto Scroll",
                                        tint = if (isAutoScrolling) Color.Black else textColor
                                    )
                                }

                                IconButton(
                                    onClick = { showAutoScrollControls = !showAutoScrollControls },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Speed,
                                        contentDescription = "Adjust Speed",
                                        tint = if (isAutoScrolling) Color.Black else secondaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Practice Session / Metronome toggle pill
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (practiceState.isPlaying) chosenChordColor else if (showPracticeSession) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .height(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            IconButton(
                                onClick = { showPracticeSession = !showPracticeSession },
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .testTag("practice_session_bottom_toggle_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = "Practice Metronome",
                                    tint = if (practiceState.isPlaying) Color.Black else if (showPracticeSession) MaterialTheme.colorScheme.onSecondaryContainer else textColor
                                )
                            }
                        }

                        // ▼ PAGE DOWN
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    // Move down by ~800 pixels (approx 1 screen)
                                    scrollState.animateScrollTo(
                                        (scrollState.value + 800).coerceAtMost(scrollState.maxValue)
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier
                                .weight(1.3f)
                                .height(56.dp)
                                .testTag("page_down_button")
                        ) {
                            Text(
                                text = AppStrings.get("page_down", langCode),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPdfExportDialog && parsedSong != null) {
        val displayKey = viewModel.getCurrentKey()
        PdfExportDialog(
            songTitle = currentSong?.title ?: parsedSong?.title ?: "Song",
            currentKey = displayKey,
            transposeSemitones = transposeSemitones,
            accidentalMode = accidentalMode,
            isExporting = isExportingPdf,
            onPrint = handlePrintPdf,
            onShare = handleSharePdf,
            onDismiss = { showPdfExportDialog = false }
        )
    }

    // Modal inspection dialog for clicked chord in lyrics or strip
    clickedChordForDetail?.let { chordName ->
        ChordDetailDialog(
            chordName = chordName,
            initialInstrument = if (selectedInstrument == InstrumentType.NONE) InstrumentType.GUITAR else selectedInstrument,
            chordColor = chosenChordColor,
            accidentalMode = accidentalMode,
            onDismiss = { clickedChordForDetail = null }
        )
    }

    if (showTempoTimeDialog) {
        val songTitle = currentSong?.title ?: parsedSong?.title ?: "Song"
        val currentTempo = currentSong?.displayTempo?.ifBlank { null } ?: parsedSong?.tempo?.ifBlank { null } ?: ""
        val currentTimeSig = currentSong?.displayTimeSignature?.ifBlank { null } ?: parsedSong?.timeSignature?.ifBlank { null } ?: ""
        TempoTimeSigDialog(
            songTitle = songTitle,
            initialTempo = currentTempo,
            initialTimeSignature = currentTimeSig,
            langCode = langCode,
            onConfirm = { tempo, timeSig ->
                viewModel.updateTempoAndTimeSignature(tempo, timeSig)
                showTempoTimeDialog = false
            },
            onDismiss = { showTempoTimeDialog = false }
        )
    }

    if (showTransliterationDialog) {
        LyricsTransliterationDialog(
            selectedTarget = transliterationTarget,
            onSelectTarget = { target ->
                viewModel.setTransliterationTarget(target)
            },
            onDismiss = { showTransliterationDialog = false }
        )
    }

    if (showSongInfoDialog) {
        val currentInfo = SongInfoData(
            title = currentSong?.title ?: parsedSong?.title ?: "",
            artist = currentSong?.artist ?: parsedSong?.artist ?: "",
            album = currentSong?.album ?: parsedSong?.album ?: "",
            key = currentSong?.originalKey ?: parsedSong?.key ?: "",
            copyright = currentSong?.copyright ?: parsedSong?.copyright ?: "",
            tempo = currentSong?.displayTempo ?: parsedSong?.tempo ?: "",
            timeSignature = currentSong?.displayTimeSignature ?: parsedSong?.timeSignature ?: "",
            capo = currentSong?.capo ?: parsedSong?.capo ?: 0
        )
        SongInfoDialog(
            initialInfo = currentInfo,
            langCode = langCode,
            onConfirm = { updatedInfo ->
                viewModel.updateSongMetadata(
                    title = updatedInfo.title,
                    artist = updatedInfo.artist,
                    album = updatedInfo.album,
                    key = updatedInfo.key,
                    copyright = updatedInfo.copyright,
                    tempo = updatedInfo.tempo,
                    timeSignature = updatedInfo.timeSignature,
                    capo = updatedInfo.capo
                )
                showSongInfoDialog = false
            },
            onDismiss = { showSongInfoDialog = false }
        )
    }

    if (showTunerDialog) {
        TunerDialog(
            accidentalMode = accidentalMode,
            onDismiss = { showTunerDialog = false }
        )
    }
}
