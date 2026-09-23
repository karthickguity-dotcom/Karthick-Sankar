package com.example.data.preferences

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ThemeOption(
    val name: String,
    val isDark: Boolean,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val defaultChordColor: Color
)

data class AppDisplaySettings(
    val themeIndex: Int = 0,
    val chordColorIndex: Int = 0,
    val lyricsFontSize: Float = 18f,
    val chordFontSize: Float = 16f,
    val lineSpacing: Float = 4f,
    val keepScreenOn: Boolean = true,
    val appLanguage: String = "en",
    val accidentalMode: String = "SHARP", // "SHARP" or "FLAT"
    val preferredInstrument: String = "GUITAR" // "GUITAR", "PIANO", "NONE"
)

class AppSettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences("app_settings_prefs", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppDisplaySettings> = _settings.asStateFlow()

    private fun loadSettings(): AppDisplaySettings {
        return AppDisplaySettings(
            themeIndex = prefs.getInt("theme_index", 0),
            chordColorIndex = prefs.getInt("chord_color_index", 0),
            lyricsFontSize = prefs.getFloat("lyrics_font_size", 18f),
            chordFontSize = prefs.getFloat("chord_font_size", 16f),
            lineSpacing = prefs.getFloat("line_spacing", 4f),
            keepScreenOn = prefs.getBoolean("keep_screen_on", true),
            appLanguage = prefs.getString("app_language", "en") ?: "en",
            accidentalMode = prefs.getString("accidental_mode", "SHARP") ?: "SHARP",
            preferredInstrument = prefs.getString("preferred_instrument", "GUITAR") ?: "GUITAR"
        )
    }

    fun updateAccidentalMode(mode: String) {
        prefs.edit().putString("accidental_mode", mode).apply()
        _settings.value = _settings.value.copy(accidentalMode = mode)
    }

    fun updatePreferredInstrument(instrument: String) {
        prefs.edit().putString("preferred_instrument", instrument).apply()
        _settings.value = _settings.value.copy(preferredInstrument = instrument)
    }

    fun updateThemeIndex(index: Int) {
        prefs.edit().putInt("theme_index", index).apply()
        _settings.value = _settings.value.copy(themeIndex = index)
    }

    fun updateChordColorIndex(index: Int) {
        prefs.edit().putInt("chord_color_index", index).apply()
        _settings.value = _settings.value.copy(chordColorIndex = index)
    }

    fun updateLyricsFontSize(size: Float) {
        prefs.edit().putFloat("lyrics_font_size", size).apply()
        _settings.value = _settings.value.copy(lyricsFontSize = size)
    }

    fun updateChordFontSize(size: Float) {
        prefs.edit().putFloat("chord_font_size", size).apply()
        _settings.value = _settings.value.copy(chordFontSize = size)
    }

    fun updateLineSpacing(spacing: Float) {
        prefs.edit().putFloat("line_spacing", spacing).apply()
        _settings.value = _settings.value.copy(lineSpacing = spacing)
    }

    fun updateKeepScreenOn(keep: Boolean) {
        prefs.edit().putBoolean("keep_screen_on", keep).apply()
        _settings.value = _settings.value.copy(keepScreenOn = keep)
    }

    fun updateSettings(transform: (AppDisplaySettings) -> AppDisplaySettings) {
        val updated = transform(_settings.value)
        _settings.value = updated
        prefs.edit()
            .putInt("theme_index", updated.themeIndex)
            .putInt("chord_color_index", updated.chordColorIndex)
            .putFloat("lyrics_font_size", updated.lyricsFontSize)
            .putFloat("chord_font_size", updated.chordFontSize)
            .putFloat("line_spacing", updated.lineSpacing)
            .putBoolean("keep_screen_on", updated.keepScreenOn)
            .putString("app_language", updated.appLanguage)
            .putString("accidental_mode", updated.accidentalMode)
            .putString("preferred_instrument", updated.preferredInstrument)
            .apply()
    }

    fun resetToDefaults() {
        val default = AppDisplaySettings()
        _settings.value = default
        prefs.edit().clear().apply()
    }

    companion object {
        val THEMES = listOf(
            ThemeOption(
                name = "Stage Dark (Default)",
                isDark = true,
                backgroundColor = Color(0xFF0F172A),
                surfaceColor = Color(0xFF1E293B),
                textColor = Color(0xFFF8FAFC),
                secondaryTextColor = Color(0xFF94A3B8),
                defaultChordColor = Color(0xFFF59E0B) // Amber
            ),
            ThemeOption(
                name = "Deep OLED Black",
                isDark = true,
                backgroundColor = Color(0xFF000000),
                surfaceColor = Color(0xFF121212),
                textColor = Color(0xFFFFFFFF),
                secondaryTextColor = Color(0xFFAAAAAA),
                defaultChordColor = Color(0xFF4ADE80) // Green
            ),
            ThemeOption(
                name = "Modern Light",
                isDark = false,
                backgroundColor = Color(0xFFF8FAFC),
                surfaceColor = Color(0xFFFFFFFF),
                textColor = Color(0xFF0F172A),
                secondaryTextColor = Color(0xFF64748B),
                defaultChordColor = Color(0xFFD97706) // Amber Dark
            ),
            ThemeOption(
                name = "Warm Sepia (Paper)",
                isDark = false,
                backgroundColor = Color(0xFFFBF0D9),
                surfaceColor = Color(0xFFF4E5C3),
                textColor = Color(0xFF3E2723),
                secondaryTextColor = Color(0xFF5D4037),
                defaultChordColor = Color(0xFFB71C1C) // Deep Red
            )
        )

        val CHORD_COLORS = listOf(
            Pair("Amber Gold", Color(0xFFF59E0B)),
            Pair("Vibrant Green", Color(0xFF22C55E)),
            Pair("Cyan Blue", Color(0xFF06B6D4)),
            Pair("Coral Orange", Color(0xFFF97316)),
            Pair("Rose Pink", Color(0xFFEC4899)),
            Pair("Electric Yellow", Color(0xFFEAB308)),
            Pair("Pure White", Color(0xFFFFFFFF)),
            Pair("Classic Red", Color(0xFFEF4444))
        )
    }
}
