package com.example

import com.example.util.metronome.PracticeSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeSessionTest {

    @Test
    fun testParseBeatsPerBar() {
        assertEquals(4, PracticeSessionState.parseBeatsPerBar("4/4"))
        assertEquals(3, PracticeSessionState.parseBeatsPerBar("3/4"))
        assertEquals(6, PracticeSessionState.parseBeatsPerBar("6/8"))
        assertEquals(2, PracticeSessionState.parseBeatsPerBar("2/4"))
        assertEquals(5, PracticeSessionState.parseBeatsPerBar("5/4"))
        assertEquals(4, PracticeSessionState.parseBeatsPerBar("invalid"))
        assertEquals(4, PracticeSessionState.parseBeatsPerBar(""))
    }

    @Test
    fun testScrollPixelsPerSecondCalculation() {
        // Standard pace at 120 BPM: (120 / 60) * 26.0 * 1.0 = 52.0 px/s
        val state120 = PracticeSessionState(bpm = 120, scrollPaceMultiplier = 1.0f)
        val speed120 = state120.getScrollPixelsPerSecond()
        assertEquals(52.0f, speed120, 0.01f)

        // Half tempo 60 BPM should scroll at exactly half speed (26.0 px/s)
        val state60 = PracticeSessionState(bpm = 60, scrollPaceMultiplier = 1.0f)
        val speed60 = state60.getScrollPixelsPerSecond()
        assertEquals(26.0f, speed60, 0.01f)

        // Pace multiplier 1.5x at 120 BPM: 52.0 * 1.5 = 78.0 px/s
        val state120Fast = PracticeSessionState(bpm = 120, scrollPaceMultiplier = 1.5f)
        val speed120Fast = state120Fast.getScrollPixelsPerSecond()
        assertEquals(78.0f, speed120Fast, 0.01f)

        // Non-positive BPM clamped to safe minimum
        val stateZero = PracticeSessionState(bpm = 0, scrollPaceMultiplier = 1.0f)
        assertTrue(stateZero.getScrollPixelsPerSecond() > 0f)
    }

    @Test
    fun testFormattedElapsedTime() {
        val state0 = PracticeSessionState(elapsedSeconds = 0)
        assertEquals("00:00", state0.formattedElapsedTime)

        val state65 = PracticeSessionState(elapsedSeconds = 65)
        assertEquals("01:05", state65.formattedElapsedTime)

        val state600 = PracticeSessionState(elapsedSeconds = 600)
        assertEquals("10:00", state600.formattedElapsedTime)
    }

    @Test
    fun testDisplayBarAndBeat() {
        val state = PracticeSessionState(currentBar = 5, currentBeat = 3, beatsPerBar = 4)
        assertEquals("Bar 5 • Beat 3/4", state.displayBarAndBeat)
    }

    @Test
    fun testStateToggleDefaults() {
        val state = PracticeSessionState(bpm = 100, isPlaying = false, isMuted = false, relativeScrollEnabled = true)
        assertFalse(state.isPlaying)
        assertFalse(state.isMuted)
        assertTrue(state.relativeScrollEnabled)

        val playingState = state.copy(isPlaying = true)
        assertTrue(playingState.isPlaying)
    }
}
