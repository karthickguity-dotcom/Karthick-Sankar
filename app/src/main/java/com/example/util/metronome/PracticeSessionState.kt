package com.example.util.metronome

data class PracticeSessionState(
    val bpm: Int = 100,
    val timeSignature: String = "4/4",
    val beatsPerBar: Int = 4,
    val isPlaying: Boolean = false,
    val currentBeat: Int = 1,
    val currentBar: Int = 1,
    val isMuted: Boolean = false,
    val countInBars: Int = 1, // 0 = none, 1 = 1 bar, 2 = 2 bars
    val isCountingIn: Boolean = false,
    val countInBeat: Int = 1,
    val countInTotalBeats: Int = 4,
    val relativeScrollEnabled: Boolean = true,
    val scrollPaceMultiplier: Float = 1.0f, // 0.5x to 2.0x pace
    val elapsedSeconds: Int = 0
) {
    val formattedElapsedTime: String
        get() {
            val minutes = elapsedSeconds / 60
            val seconds = elapsedSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val displayBarAndBeat: String
        get() = if (isCountingIn) {
            "Count-in: Beat $countInBeat / $countInTotalBeats"
        } else {
            "Bar $currentBar • Beat $currentBeat/$beatsPerBar"
        }

    /**
     * Computes the relative scroll velocity in pixels per second.
     * Normalized around standard 26 pixels per beat at 1.0x pace.
     */
    fun getScrollPixelsPerSecond(): Float {
        val beatsPerSecond = bpm.coerceAtLeast(30) / 60f
        val basePixelsPerBeat = 26f
        return beatsPerSecond * basePixelsPerBeat * scrollPaceMultiplier
    }

    companion object {
        fun parseBeatsPerBar(timeSignature: String): Int {
            val clean = timeSignature.trim()
            val numerator = clean.substringBefore("/").toIntOrNull()
            return when {
                numerator != null && numerator in 1..16 -> numerator
                clean.startsWith("3") -> 3
                clean.startsWith("6") -> 6
                clean.startsWith("2") -> 2
                else -> 4
            }
        }
    }
}
