package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.data.parser.AccidentalMode
import com.example.util.tuner.AudioTunerEngine
import com.example.util.tuner.GuitarStringNote
import com.example.util.tuner.PitchDetector
import com.example.util.tuner.TunerMode
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TunerDialog(
    accidentalMode: AccidentalMode,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val engine = remember { AudioTunerEngine(scope) }
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

    // Synchronize accidental mode with song mode
    LaunchedEffect(accidentalMode) {
        engine.setAccidentalMode(accidentalMode)
    }

    // Auto-start listening if permission already granted
    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission) {
            engine.startListening()
        }
    }

    // Clean up when dialog closes
    DisposableEffect(Unit) {
        onDispose {
            engine.release()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .testTag("tuner_dialog"),
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
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🎛️", fontSize = 18.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Instrument Tuner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real-Time Pitch Detection",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("tuner_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Tuner"
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!hasAudioPermission) {
                    // Microphone Permission Request Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Microphone Access Required",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "To accurately detect your instrument's pitch, please allow microphone access.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { permissionLauncher.launch(Manifest.permission.RECORD_AUDIO) },
                                modifier = Modifier.testTag("tuner_grant_permission_button")
                            ) {
                                Text("Grant Permission")
                            }
                        }
                    }
                } else {
                    // 1. Tuner Mode Selector (Chromatic vs Guitar Standard)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        FilterChip(
                            selected = tunerState.mode == TunerMode.CHROMATIC,
                            onClick = { engine.setMode(TunerMode.CHROMATIC) },
                            label = { Text("Chromatic (Piano/All)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tuner_mode_chromatic")
                        )

                        FilterChip(
                            selected = tunerState.mode == TunerMode.GUITAR,
                            onClick = { engine.setMode(TunerMode.GUITAR) },
                            label = { Text("🎸 Guitar (Standard)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tuner_mode_guitar")
                        )
                    }

                    // 2. Guitar String Selector Pills (if in Guitar mode)
                    AnimatedVisibility(visible = tunerState.mode == TunerMode.GUITAR) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Select Target String:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
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
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 2.dp)
                                            .testTag("guitar_string_${stringNote.stringNumber}")
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${stringNote.stringNumber}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = "${stringNote.noteName}${stringNote.octave}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // 3. Tuner Meter Dial Canvas
                    TunerDialGauge(
                        cents = tunerState.cents,
                        isInTune = tunerState.isInTune,
                        isListening = tunerState.isListening && tunerState.frequency > 0f
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Large Note Display & Frequency Details
                    val statusColor by animateColorAsState(
                        targetValue = when {
                            !tunerState.isListening || tunerState.frequency <= 0f -> MaterialTheme.colorScheme.onSurfaceVariant
                            tunerState.isInTune -> Color(0xFF10B981) // Emerald Green
                            tunerState.cents < 0f -> Color(0xFFF59E0B) // Amber (Flat)
                            else -> Color(0xFFEF4444) // Red/Orange (Sharp)
                        },
                        animationSpec = tween(200)
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = statusColor.copy(alpha = 0.12f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = if (tunerState.frequency > 0f) tunerState.noteName else "--",
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Black,
                                    color = statusColor,
                                    modifier = Modifier.testTag("tuner_note_display")
                                )
                                if (tunerState.frequency > 0f && tunerState.octave > 0) {
                                    Text(
                                        text = "${tunerState.octave}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusColor.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(bottom = 8.dp, start = 2.dp)
                                    )
                                }
                            }

                            val tuningStatusText = when {
                                !tunerState.isListening -> "Tuner Paused"
                                tunerState.frequency <= 0f -> "Listening for sound..."
                                tunerState.isInTune -> "✓ IN TUNE"
                                tunerState.cents < -3.5f -> "▼ FLAT (Tune Up)"
                                else -> "▲ SHARP (Tune Down)"
                            }

                            Text(
                                text = tuningStatusText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )

                            if (tunerState.frequency > 0f) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Current: ${"%.1f".format(tunerState.frequency)} Hz  •  Target: ${"%.1f".format(tunerState.targetFrequency)} Hz",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val centsSigned = if (tunerState.cents > 0) "+%.1f".format(tunerState.cents) else "%.1f".format(tunerState.cents)
                                Text(
                                    text = "$centsSigned cents",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Action Buttons (Toggle Mic & Play Reference Tone)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (tunerState.isListening) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                                contentColor = if (tunerState.isListening) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tuner_toggle_mic")
                        ) {
                            Icon(
                                imageVector = if (tunerState.isListening) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (tunerState.isListening) "Pause Mic" else "Resume Mic",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // Reference Tone Generator Button
                        val targetToneFreq = if (tunerState.targetFrequency > 0f) tunerState.targetFrequency else 440f
                        Surface(
                            onClick = {
                                if (tunerState.isPlayingTone) {
                                    engine.stopTone()
                                } else {
                                    engine.playReferenceTone(targetToneFreq)
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (tunerState.isPlayingTone) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("tuner_reference_tone")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = if (tunerState.isPlayingTone) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (tunerState.isPlayingTone) "Stop Tone" else "Hear Tone",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (tunerState.isPlayingTone) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("tuner_dialog_done")
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}

/**
 * Visual arc gauge needle displaying -50 to +50 cents deviation.
 */
@Composable
fun TunerDialGauge(
    cents: Float,
    isInTune: Boolean,
    isListening: Boolean
) {
    val animatedCents by animateFloatAsState(
        targetValue = if (isListening) cents.coerceIn(-50f, 50f) else 0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing)
    )

    val gaugeColor = when {
        !isListening -> MaterialTheme.colorScheme.outlineVariant
        isInTune -> Color(0xFF10B981) // In Tune Green
        animatedCents < 0f -> Color(0xFFF59E0B) // Amber
        else -> Color(0xFFEF4444) // Sharp Red
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(90.dp)) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val centerY = h * 0.92f
            val radius = minOf(w * 0.42f, h * 1.5f)

            // Arc span: from -60 degrees to +60 degrees from vertical (total 120 degrees)
            // Draw background tick marks (-50, -25, 0, +25, +50)
            val ticks = listOf(-50f, -37.5f, -25f, -12.5f, 0f, 12.5f, 25f, 37.5f, 50f)

            ticks.forEach { tickCents ->
                val angleDeg = 270f + (tickCents / 50f) * 55f
                val rad = Math.toRadians(angleDeg.toDouble())
                val isMajor = tickCents % 25f == 0f
                val isCenter = tickCents == 0f

                val tickLen = if (isCenter) 18f else if (isMajor) 12f else 6f
                val startR = radius - tickLen
                val endR = radius

                val start = Offset(
                    (centerX + startR * cos(rad)).toFloat(),
                    (centerY + startR * sin(rad)).toFloat()
                )
                val end = Offset(
                    (centerX + endR * cos(rad)).toFloat(),
                    (centerY + endR * sin(rad)).toFloat()
                )

                val tickColor = when {
                    isCenter -> Color(0xFF10B981)
                    isMajor -> Color(0xFF94A3B8)
                    else -> Color(0xFFCBD5E1)
                }

                drawLine(
                    color = tickColor,
                    start = start,
                    end = end,
                    strokeWidth = if (isCenter) 3.5f else if (isMajor) 2f else 1f,
                    cap = StrokeCap.Round
                )
            }

            // Draw center "Sweet Spot" target indicator
            val centerRad = Math.toRadians(270.0)
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = if (isInTune) 0.9f else 0.35f),
                radius = if (isInTune) 6f else 4f,
                center = Offset(
                    (centerX + (radius - 20f) * cos(centerRad)).toFloat(),
                    (centerY + (radius - 20f) * sin(centerRad)).toFloat()
                )
            )

            // Draw Needle
            val needleAngleDeg = 270f + (animatedCents / 50f) * 55f
            val needleRad = Math.toRadians(needleAngleDeg.toDouble())
            val needleEnd = Offset(
                (centerX + (radius - 4f) * cos(needleRad)).toFloat(),
                (centerY + (radius - 4f) * sin(needleRad)).toFloat()
            )

            // Needle line
            drawLine(
                color = gaugeColor,
                start = Offset(centerX, centerY),
                end = needleEnd,
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Pivot circle at base
            drawCircle(
                color = gaugeColor,
                radius = 6f,
                center = Offset(centerX, centerY)
            )
        }
    }
}
