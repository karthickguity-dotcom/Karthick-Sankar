package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.metronome.PracticeSessionState

@Composable
fun PracticeSessionPanel(
    state: PracticeSessionState,
    chordColor: Color,
    onTogglePlay: () -> Unit,
    onResetSession: () -> Unit,
    onAdjustBpm: (Int) -> Unit,
    onSetBpm: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onSetTimeSignature: (String) -> Unit,
    onToggleMute: () -> Unit,
    onToggleRelativeScroll: () -> Unit,
    onSetScrollPace: (Float) -> Unit,
    onSetCountInBars: (Int) -> Unit,
    onNudgeScroll: (pixels: Float) -> Unit,
    onScrollToTop: () -> Unit,
    onSaveBpmToSong: () -> Unit,
    onOpenTuner: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var saveFeedback by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 10.dp,
        shadowElevation = 14.dp,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = chordColor.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("practice_session_panel")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header Row: Title, Bar & Elapsed Counter, Minimize/Close
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(chordColor.copy(alpha = 0.15f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = chordColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Practice Session",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (state.isCountingIn) {
                                "Count-in: Beat ${state.countInBeat} / ${state.countInTotalBeats}"
                            } else {
                                "${state.displayBarAndBeat} • ${state.formattedElapsedTime}"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (state.isCountingIn) chordColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onOpenTuner != null) {
                        IconButton(
                            onClick = onOpenTuner,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("practice_tuner_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Instrument Tuner",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("practice_mute_button")
                    ) {
                        Icon(
                            imageVector = if (state.isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                            contentDescription = if (state.isMuted) "Unmute" else "Mute",
                            tint = if (state.isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("practice_expand_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse" else "Expand",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("practice_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Practice Panel",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Metronome Beat Indicator Strip
            MetronomeBeatStrip(
                beatsPerBar = state.beatsPerBar,
                currentBeat = state.currentBeat,
                isCountingIn = state.isCountingIn,
                countInBeat = state.countInBeat,
                isPlaying = state.isPlaying,
                chordColor = chordColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Controls: BPM Steppers, Big BPM Display, TAP button, Play/Pause
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Play / Pause Master Button
                FilledTonalButton(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.isPlaying) chordColor else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (state.isPlaying) Color.Black else MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("practice_play_pause_button")
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause Metronome" else "Start Metronome",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.isPlaying) "Pause" else "Practice",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // BPM Steppers Row: [-5] [-1] [ BPM ] [+1] [+5]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // -5
                    FilledTonalButton(
                        onClick = { onAdjustBpm(-5) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("practice_bpm_minus_5")
                    ) {
                        Text("-5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // -1
                    FilledTonalButton(
                        onClick = { onAdjustBpm(-1) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("practice_bpm_minus_1")
                    ) {
                        Text("-1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Big BPM Text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .testTag("practice_current_bpm")
                    ) {
                        Text(
                            text = "${state.bpm}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = chordColor
                        )
                        Text(
                            text = "BPM",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // +1
                    FilledTonalButton(
                        onClick = { onAdjustBpm(1) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("practice_bpm_plus_1")
                    ) {
                        Text("+1", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // +5
                    FilledTonalButton(
                        onClick = { onAdjustBpm(5) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("practice_bpm_plus_5")
                    ) {
                        Text("+5", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // TAP Tempo Button
                Button(
                    onClick = onTapTempo,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("practice_tap_tempo_button")
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "TAP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // Quick Relative Scroll Status Pill
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        tint = if (state.relativeScrollEnabled) chordColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.relativeScrollEnabled) {
                            "Relative Page Scroll: Active (${state.scrollPaceMultiplier}x pace)"
                        } else {
                            "Relative Page Scroll: Paused"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Switch(
                    checked = state.relativeScrollEnabled,
                    onCheckedChange = { onToggleRelativeScroll() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = chordColor,
                        checkedTrackColor = chordColor.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .scale(0.8f)
                        .testTag("practice_relative_scroll_toggle")
                )
            }

            // EXPANDED ADVANCED CONTROLS
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    // 1. Time Signature Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Meter / Time Signature:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("4/4", "3/4", "6/8", "2/4").forEach { meter ->
                                FilterChip(
                                    selected = state.timeSignature == meter,
                                    onClick = { onSetTimeSignature(meter) },
                                    label = { Text(meter, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    modifier = Modifier.testTag("meter_chip_$meter")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Count-In Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Count-in Before Scroll:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(0 to "None", 1 to "1 Bar", 2 to "2 Bars").forEach { (bars, label) ->
                                FilterChip(
                                    selected = state.countInBars == bars,
                                    onClick = { onSetCountInBars(bars) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("count_in_chip_$bars")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Relative Scroll Pace Slider (Lines / Bars ratio)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Relative Scroll Pace Multiplier",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "%.2fx (%.1f px/s)".format(state.scrollPaceMultiplier, state.getScrollPixelsPerSecond()),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = chordColor
                            )
                        }

                        Slider(
                            value = state.scrollPaceMultiplier,
                            onValueChange = onSetScrollPace,
                            valueRange = 0.4f..2.2f,
                            steps = 17,
                            modifier = Modifier.testTag("practice_pace_slider")
                        )

                        // Quick pace preset chips
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                0.6f to "0.6x Slow",
                                0.8f to "0.8x",
                                1.0f to "1.0x Normal",
                                1.3f to "1.3x",
                                1.6f to "1.6x Fast"
                            ).forEach { (pace, label) ->
                                FilterChip(
                                    selected = kotlin.math.abs(state.scrollPaceMultiplier - pace) < 0.05f,
                                    onClick = { onSetScrollPace(pace) },
                                    label = { Text(label, fontSize = 10.sp) },
                                    modifier = Modifier.testTag("pace_chip_$pace")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Page Nudge / Sync Positioning Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Nudge Back 1 Bar / Line
                        OutlinedButton(
                            onClick = { onNudgeScroll(-120f) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("practice_nudge_up_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nudge Up", fontSize = 11.sp)
                        }

                        // Nudge Forward 1 Bar / Line
                        OutlinedButton(
                            onClick = { onNudgeScroll(120f) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("practice_nudge_down_button")
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Nudge Down", fontSize = 11.sp)
                        }

                        // Scroll back to top
                        OutlinedButton(
                            onClick = onScrollToTop,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("practice_scroll_top_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Top", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Actions: Reset Session, Save BPM to Song
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(
                            onClick = onResetSession,
                            modifier = Modifier.testTag("practice_reset_session_button")
                        ) {
                            Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset Counter", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = {
                                onSaveBpmToSong()
                                saveFeedback = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("practice_save_bpm_to_song_button")
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (saveFeedback) "Saved ${state.bpm} BPM!" else "Save BPM to Song",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated beat dots showing metronome pulsing rhythm.
 * Accents Beat 1 with larger size and distinctive highlight.
 */
@Composable
private fun MetronomeBeatStrip(
    beatsPerBar: Int,
    currentBeat: Int,
    isCountingIn: Boolean,
    countInBeat: Int,
    isPlaying: Boolean,
    chordColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp, horizontal = 12.dp)
            .testTag("metronome_beat_strip")
    ) {
        val totalBeats = beatsPerBar.coerceIn(1, 12)
        val activeBeat = if (isCountingIn) {
            ((countInBeat - 1) % beatsPerBar) + 1
        } else {
            currentBeat
        }

        for (b in 1..totalBeats) {
            val isCurrent = isPlaying && (activeBeat == b)
            val isDownbeat = (b == 1)

            val animatedScale by animateFloatAsState(
                targetValue = if (isCurrent) 1.35f else 1.0f,
                animationSpec = tween(durationMillis = 90, easing = FastOutSlowInEasing),
                label = "beatScale"
            )

            val dotColor by animateColorAsState(
                targetValue = when {
                    isCurrent && isDownbeat -> chordColor
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isDownbeat -> chordColor.copy(alpha = 0.45f)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                },
                animationSpec = tween(durationMillis = 80),
                label = "beatColor"
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .scale(animatedScale)
                    .size(if (isDownbeat) 26.dp else 22.dp)
                    .clip(CircleShape)
                    .background(dotColor)
                    .border(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = "$b",
                    fontSize = if (isDownbeat) 12.sp else 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCurrent) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
