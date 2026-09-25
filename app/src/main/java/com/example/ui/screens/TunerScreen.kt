package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.parser.AccidentalMode
import com.example.ui.components.TunerDialGauge
import com.example.ui.i18n.AppStrings
import com.example.util.tuner.AudioTunerEngine
import com.example.util.tuner.GuitarStringNote
import com.example.util.tuner.PitchDetector
import com.example.util.tuner.TunerMode
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TunerScreen(
    langCode: String,
    accidentalMode: AccidentalMode,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler(onBack = onBack)

    // Engine is instantiated and managed strictly within this screen's lifecycle
    val engine = remember { AudioTunerEngine(coroutineScope) }
    val tunerState by engine.uiState.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            engine.startListening()
        }
    }

    // Synchronize accidental mode
    LaunchedEffect(accidentalMode) {
        engine.setAccidentalMode(accidentalMode)
    }

    // Auto-start listening if permission already granted
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            engine.startListening()
        }
    }

    // Critical: Clean up audio resources completely when navigating away or closing
    DisposableEffect(Unit) {
        onDispose {
            engine.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = AppStrings.get("instrument_tuner", langCode),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-Time Pitch Detection & Reference Tones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("tuner_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Accidental mode switch (♯ / ♭)
                    Row(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(2.dp)
                    ) {
                        Surface(
                            onClick = { engine.setAccidentalMode(AccidentalMode.SHARP) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (tunerState.accidentalMode == AccidentalMode.SHARP) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.testTag("tuner_screen_sharp_toggle")
                        ) {
                            Text(
                                text = "♯ Sharp",
                                fontSize = 12.sp,
                                fontWeight = if (tunerState.accidentalMode == AccidentalMode.SHARP) FontWeight.Bold else FontWeight.Medium,
                                color = if (tunerState.accidentalMode == AccidentalMode.SHARP) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            onClick = { engine.setAccidentalMode(AccidentalMode.FLAT) },
                            shape = RoundedCornerShape(6.dp),
                            color = if (tunerState.accidentalMode == AccidentalMode.FLAT) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier.testTag("tuner_screen_flat_toggle")
                        ) {
                            Text(
                                text = "♭ Flat",
                                fontSize = 12.sp,
                                fontWeight = if (tunerState.accidentalMode == AccidentalMode.FLAT) FontWeight.Bold else FontWeight.Medium,
                                color = if (tunerState.accidentalMode == AccidentalMode.FLAT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!hasAudioPermission) {
                        // Permission Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp)
                                .testTag("tuner_permission_card")
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = CircleShape,
                                    modifier = Modifier.size(56.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Mic,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Microphone Permission Required",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "To accurately detect your musical instrument's pitch, microphone access is required. Audio is analyzed purely in real-time on your device and is never recorded or saved.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("tuner_request_permission_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Grant Microphone Access", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // 1. Tuner Mode Selector
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            FilterChip(
                                selected = tunerState.mode == TunerMode.GUITAR,
                                onClick = { engine.setMode(TunerMode.GUITAR) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎸", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Guitar (Standard)", fontWeight = FontWeight.Bold)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("tuner_mode_guitar_chip")
                            )

                            FilterChip(
                                selected = tunerState.mode == TunerMode.CHROMATIC,
                                onClick = { engine.setMode(TunerMode.CHROMATIC) },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("🎹", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Chromatic (All)", fontWeight = FontWeight.Bold)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("tuner_mode_chromatic_chip")
                            )
                        }

                        // 2. Guitar String Selector Pills (Guitar Mode)
                        AnimatedVisibility(visible = tunerState.mode == TunerMode.GUITAR) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp)
                                ) {
                                    Text(
                                        text = "Target String Selection:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "E A D G B E",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    PitchDetector.GUITAR_STANDARD_STRINGS.forEach { stringNote ->
                                        val isSelected = tunerState.selectedGuitarString?.stringNumber == stringNote.stringNumber
                                        Surface(
                                            onClick = {
                                                engine.selectGuitarString(stringNote)
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(horizontal = 2.5.dp)
                                                .testTag("tuner_screen_string_${stringNote.stringNumber}")
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            ) {
                                                Text(
                                                    text = "Str ${stringNote.stringNumber}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "${stringNote.noteName}${stringNote.octave}",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = "${stringNote.frequency.toInt()}Hz",
                                                    fontSize = 10.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Precision Tuner Needle Arc Gauge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                            ) {
                                TunerDialGauge(
                                    cents = tunerState.cents,
                                    isInTune = tunerState.isInTune,
                                    isListening = tunerState.isListening && tunerState.frequency > 0f
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .padding(top = 4.dp)
                                ) {
                                    Text("-50¢", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("-25¢", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("IN TUNE (0)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                    Text("+25¢", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("+50¢", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 4. Large Note Display Card & Pitch Telemetry
                        val statusColor by animateColorAsState(
                            targetValue = when {
                                !tunerState.isListening || tunerState.frequency <= 0f -> MaterialTheme.colorScheme.onSurfaceVariant
                                tunerState.isInTune -> Color(0xFF10B981) // Emerald Green
                                tunerState.cents < 0f -> Color(0xFFF59E0B) // Amber
                                else -> Color(0xFFEF4444) // Sharp Red
                            },
                            animationSpec = tween(180)
                        )

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = statusColor.copy(alpha = 0.08f)
                            ),
                            border = BorderStroke(2.dp, statusColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tuner_screen_note_card")
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (tunerState.frequency > 0f) tunerState.noteName else "--",
                                        fontSize = 72.sp,
                                        fontWeight = FontWeight.Black,
                                        color = statusColor,
                                        modifier = Modifier.testTag("tuner_screen_main_note")
                                    )
                                    if (tunerState.frequency > 0f && tunerState.octave > 0) {
                                        Text(
                                            text = "${tunerState.octave}",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor.copy(alpha = 0.85f),
                                            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                                        )
                                    }
                                }

                                val statusHeadline = when {
                                    !tunerState.isListening -> "Tuner Paused"
                                    tunerState.frequency <= 0f -> "Listening for sound..."
                                    tunerState.isInTune -> "✓ PERFECT PITCH (IN TUNE)"
                                    tunerState.cents < -3.5f -> "▼ TUNE UP (FLAT)"
                                    else -> "▲ TUNE DOWN (SHARP)"
                                }

                                Text(
                                    text = statusHeadline,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor
                                )

                                if (tunerState.frequency > 0f) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Text(
                                                text = "Current: ${"%.1f".format(tunerState.frequency)} Hz",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        ) {
                                            Text(
                                                text = "Target: ${"%.1f".format(tunerState.targetFrequency)} Hz",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    val centsSigned = if (tunerState.cents > 0) "+%.1f".format(tunerState.cents) else "%.1f".format(tunerState.cents)
                                    Text(
                                        text = "$centsSigned cents deviation",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // 5. Controls: Pause/Resume Mic + Hear Reference Tone
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    if (tunerState.isListening) {
                                        engine.stopListening()
                                    } else {
                                        engine.startListening()
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tunerState.isListening) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                                    contentColor = if (tunerState.isListening) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("tuner_screen_mic_toggle")
                            ) {
                                Icon(
                                    imageVector = if (tunerState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (tunerState.isListening) "Pause Mic" else "Resume Mic",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            val targetToneFreq = if (tunerState.targetFrequency > 0f) tunerState.targetFrequency else 440f
                            Button(
                                onClick = {
                                    if (tunerState.isPlayingTone) {
                                        engine.stopTone()
                                    } else {
                                        engine.playReferenceTone(targetToneFreq)
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tunerState.isPlayingTone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (tunerState.isPlayingTone) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("tuner_screen_reference_tone_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (tunerState.isPlayingTone) "Stop Tone" else "Hear Tone 🔊",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 6. Pro-Tips Card
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Tuning Tips",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "• Pluck a single string clearly with your thumb or pick.\n" +
                                           "• Hold the instrument near your phone's microphone for best acoustic clarity.\n" +
                                           "• In Guitar mode, tap individual strings to lock the tuner to that string's frequency.\n" +
                                           "• Use Chromatic mode for Piano keys, vocals, or non-standard guitar tunings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
