package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.parser.AccidentalMode
import com.example.data.preferences.AppSettingsManager
import com.example.data.repository.SongRepository
import com.example.ui.screens.ConverterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LyricsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TunerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LyricsViewModel
import com.example.ui.viewmodel.SongLibraryViewModel

sealed interface AppScreen {
    data object Home : AppScreen
    data class Lyrics(val songId: Long) : AppScreen
    data object Settings : AppScreen
    data object Converter : AppScreen
    data class Tuner(val returnScreen: AppScreen = AppScreen.Home) : AppScreen
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(applicationContext)
        val repository = SongRepository(database.songDao(), applicationContext)
        val settingsManager = AppSettingsManager(applicationContext)

        setContent {
            val settings by settingsManager.settings.collectAsStateWithLifecycle()
            val activeTheme = AppSettingsManager.THEMES.getOrElse(settings.themeIndex) {
                AppSettingsManager.THEMES[0]
            }

            MyApplicationTheme(
                darkTheme = activeTheme.isDark,
                dynamicColor = false
            ) {
                var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

                val songViewModel = remember { SongLibraryViewModel(repository) }
                val lyricsViewModel = remember { LyricsViewModel(repository) }

                // Handle hardware or system gesture Back button
                BackHandler(enabled = currentScreen !is AppScreen.Home) {
                    currentScreen = when (val s = currentScreen) {
                        is AppScreen.Tuner -> s.returnScreen
                        else -> AppScreen.Home
                    }
                }

                Surface(
                    color = activeTheme.backgroundColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    when (val screen = currentScreen) {
                        is AppScreen.Home -> {
                            HomeScreen(
                                viewModel = songViewModel,
                                langCode = settings.appLanguage,
                                onSongSelected = { songId ->
                                    currentScreen = AppScreen.Lyrics(songId)
                                },
                                onNavigateSettings = {
                                    currentScreen = AppScreen.Settings
                                },
                                onNavigateConverter = {
                                    currentScreen = AppScreen.Converter
                                },
                                onNavigateTuner = {
                                    currentScreen = AppScreen.Tuner(returnScreen = AppScreen.Home)
                                }
                            )
                        }
                        is AppScreen.Lyrics -> {
                            LyricsScreen(
                                songId = screen.songId,
                                viewModel = lyricsViewModel,
                                settings = settings,
                                langCode = settings.appLanguage,
                                onUpdateAccidentalMode = { mode ->
                                    settingsManager.updateAccidentalMode(mode)
                                },
                                onUpdatePreferredInstrument = { instrument ->
                                    settingsManager.updatePreferredInstrument(instrument)
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                onNavigateSettings = {
                                    currentScreen = AppScreen.Settings
                                },
                                onNavigateTuner = {
                                    currentScreen = AppScreen.Tuner(returnScreen = AppScreen.Lyrics(screen.songId))
                                }
                            )
                        }
                        is AppScreen.Settings -> {
                            SettingsScreen(
                                settings = settings,
                                onUpdateSettings = { transform ->
                                    settingsManager.updateSettings(transform)
                                },
                                onResetDefaults = {
                                    settingsManager.resetToDefaults()
                                },
                                onBack = {
                                    currentScreen = AppScreen.Home
                                }
                            )
                        }
                        is AppScreen.Converter -> {
                            ConverterScreen(
                                langCode = settings.appLanguage,
                                onBack = {
                                    currentScreen = AppScreen.Home
                                },
                                onSaveToLibrary = { chordProText, title ->
                                    songViewModel.importFromText(chordProText, title) {
                                        currentScreen = AppScreen.Home
                                    }
                                }
                            )
                        }
                        is AppScreen.Tuner -> {
                            TunerScreen(
                                langCode = settings.appLanguage,
                                accidentalMode = AccidentalMode.fromString(settings.accidentalMode),
                                onBack = {
                                    currentScreen = screen.returnScreen
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
