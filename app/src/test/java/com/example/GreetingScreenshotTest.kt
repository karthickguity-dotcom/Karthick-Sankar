package com.example

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.parser.ChordProParser
import com.example.ui.components.ChordProSongView
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun chordpro_screenshot() {
    val sampleSong = ChordProParser.parse("[G]Amazing [C]grace how [G]sweet the sound", "Amazing Grace")

    composeTestRule.setContent {
      MyApplicationTheme {
        ChordProSongView(
          parsedSong = sampleSong,
          transposeSemitones = 0,
          lyricsFontSize = 18f,
          chordFontSize = 16f,
          lineSpacing = 6f,
          chordColor = Color(0xFFF59E0B),
          lyricsColor = Color.White
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/chordpro_lyrics.png")
  }
}
