package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.local.SongDao
import com.example.data.model.SongEntity
import com.example.data.parser.ChordProParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SongRepository(
    private val songDao: SongDao,
    private val context: Context
) {

    val allSongs: Flow<List<SongEntity>> = songDao.getAllSongs()

    fun searchSongs(query: String): Flow<List<SongEntity>> = songDao.searchSongs(query)

    fun getSongById(id: Long): Flow<SongEntity?> = songDao.getSongById(id)

    suspend fun getSongByIdDirect(id: Long): SongEntity? = withContext(Dispatchers.IO) {
        songDao.getSongByIdDirect(id)
    }

    suspend fun insertSong(song: SongEntity): Long = withContext(Dispatchers.IO) {
        songDao.insertSong(song)
    }

    suspend fun updateSong(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.updateSong(song)
    }

    suspend fun renameSong(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        songDao.renameSong(id, newTitle)
    }

    suspend fun toggleFavorite(id: Long) = withContext(Dispatchers.IO) {
        songDao.toggleFavorite(id)
    }

    suspend fun deleteSong(song: SongEntity) = withContext(Dispatchers.IO) {
        songDao.deleteSong(song)
    }

    suspend fun deleteSongById(id: Long) = withContext(Dispatchers.IO) {
        songDao.deleteSongById(id)
    }

    /**
     * Imports a ChordPro file from content Uri.
     */
    suspend fun importFromUri(uri: Uri): Result<SongEntity> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                stream.bufferedReader(Charsets.UTF_8).readText()
            } ?: return@withContext Result.failure(Exception("Cannot read file content"))

            if (content.isBlank()) {
                return@withContext Result.failure(Exception("The selected file is empty"))
            }

            // Extract filename as fallback title
            var fallbackTitle = "Imported Song"
            uri.lastPathSegment?.let { segment ->
                val name = segment.substringAfterLast("/").substringBeforeLast(".")
                if (name.isNotBlank()) fallbackTitle = name
            }

            val parsed = ChordProParser.parse(content, fallbackTitle)
            val entity = SongEntity(
                title = parsed.title.ifBlank { fallbackTitle },
                artist = parsed.artist,
                album = parsed.album,
                originalKey = parsed.key,
                capo = parsed.capo,
                tempo = parsed.tempo,
                timeSignature = parsed.timeSignature,
                copyright = parsed.copyright,
                rawChordPro = content
            )

            val id = songDao.insertSong(entity)
            Result.success(entity.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Imports a song from pasted raw ChordPro text.
     */
    suspend fun importFromText(rawText: String, defaultTitle: String = "My Song"): Result<SongEntity> = withContext(Dispatchers.IO) {
        try {
            if (rawText.isBlank()) {
                return@withContext Result.failure(Exception("Text cannot be empty"))
            }

            val userTitle = defaultTitle.trim()
            val parsed = ChordProParser.parse(rawText, if (userTitle.isNotBlank()) userTitle else "My Song")
            val finalTitle = if (userTitle.isNotBlank()) userTitle else parsed.title.ifBlank { "My Song" }

            // Ensure ChordPro text has the user's title directive synchronized
            val titleRegex = Regex("""^(\s*\{\s*(?:title|t)\s*:\s*)(.*?)\s*\}\s*$""", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
            val updatedChordPro = if (titleRegex.containsMatchIn(rawText)) {
                titleRegex.replace(rawText) { matchResult ->
                    "${matchResult.groupValues[1]}$finalTitle}"
                }
            } else {
                "{title: $finalTitle}\n$rawText"
            }

            val entity = SongEntity(
                title = finalTitle,
                artist = parsed.artist,
                album = parsed.album,
                originalKey = parsed.key,
                capo = parsed.capo,
                tempo = parsed.tempo,
                timeSignature = parsed.timeSignature,
                copyright = parsed.copyright,
                rawChordPro = updatedChordPro,
                updatedAt = System.currentTimeMillis()
            )

            val id = songDao.insertSong(entity)
            Result.success(entity.copy(id = id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Ensures default sample songs exist in database upon first launch.
     */
    suspend fun prepopulateDefaultSongsIfNeeded() = withContext(Dispatchers.IO) {
        if (songDao.getSongCount() == 0) {
            val defaultSongs = listOf(
                createSampleSong(
                    title = "Amazing Grace",
                    artist = "John Newton",
                    key = "G",
                    capo = 2,
                    raw = AMAZING_GRACE_CHORDPRO
                ),
                createSampleSong(
                    title = "How Great Thou Art",
                    artist = "Stuart K. Hine",
                    key = "C",
                    capo = 0,
                    raw = HOW_GREAT_THOU_ART_CHORDPRO
                ),
                createSampleSong(
                    title = "En Manam Engum",
                    artist = "Tamil Worship",
                    key = "D",
                    capo = 0,
                    raw = EN_MANAM_ENGUM_CHORDPRO
                ),
                createSampleSong(
                    title = "Yesu Tera Naam",
                    artist = "Hindi Worship",
                    key = "E",
                    capo = 0,
                    raw = YESU_TERA_NAAM_CHORDPRO
                ),
                createSampleSong(
                    title = "Preminchedhan",
                    artist = "Telugu Worship",
                    key = "G",
                    capo = 0,
                    raw = PREMINCHEDHAN_CHORDPRO
                ),
                createSampleSong(
                    title = "Sthuthikku Paathran",
                    artist = "Malayalam Worship",
                    key = "A",
                    capo = 0,
                    raw = STHUTHIKKU_PAATHRAN_CHORDPRO
                ),
                createSampleSong(
                    title = "Nanna Aathmavu",
                    artist = "Kannada Worship",
                    key = "D",
                    capo = 0,
                    raw = NANNA_AATHMAVU_CHORDPRO
                )
            )
            songDao.insertSongs(defaultSongs)
        }
    }

    private fun createSampleSong(
        title: String,
        artist: String,
        key: String,
        capo: Int,
        raw: String
    ): SongEntity {
        val parsed = ChordProParser.parse(raw, title)
        return SongEntity(
            title = title,
            artist = artist,
            album = parsed.album,
            originalKey = key,
            capo = capo,
            tempo = parsed.tempo,
            timeSignature = parsed.timeSignature,
            copyright = parsed.copyright,
            rawChordPro = raw
        )
    }

    companion object {
        val AMAZING_GRACE_CHORDPRO = """
            {title: Amazing Grace}
            {artist: John Newton}
            {album: Olney Hymns}
            {key: G}
            {capo: 2}
            {tempo: 72}
            {time: 3/4}
            {copyright: Public Domain • Words: John Newton (1779)}

            {comment: Verse 1}
            [G] Amazing grace how [C]sweet the [G]sound
            That saved a [D]wretch like [G]me
            [G] I once was [C]lost but [G]now am [D]found
            Was [G]blind but [C]now I [G]see

            {comment: Verse 2}
            'Twas [G]grace that taught my [C]heart to [G]fear
            And grace my [D]fears re[G]lieved
            How [G]precious [C]did that [G]grace ap[D]pear
            The [G]hour I [C]first be[G]lieved

            {comment: Chorus}
            {soc}
            My [C]chains are gone, I've [G/B]been set free
            My [C/E]God my Savior has [G/D]ransomed me
            And [C]like a flood His [G/B]mercy rains
            Un[Am7]ending love, a[D7]mazing [G]grace
            {eoc}

            {comment: Verse 3}
            Through [G]many dangers, [C]toils, and [G]snares
            I have al[D]ready [G]come
            'Tis [G]grace hath [C]brought me [G]safe thus [D]far
            And [G]grace will [C]lead me [G]home
        """.trimIndent()

        val HOW_GREAT_THOU_ART_CHORDPRO = """
            {title: How Great Thou Art}
            {artist: Stuart K. Hine}
            {key: C}
            {tempo: 78}
            {time: 4/4}

            {comment: Verse 1}
            O [C]Lord my God, when I in [F]awesome wonder
            Con[C]sider all the [G7]worlds Thy hands have [C]made
            I see the stars, I hear the [F]rolling thunder
            Thy [C]power through[G7]out the universe dis[C]played

            {comment: Chorus}
            {soc}
            Then sings my [C]soul, my [F]Savior God to [C]Thee
            How great Thou [Dm7]art, [G7]how great Thou [C]art
            Then sings my [C]soul, my [F]Savior God to [C]Thee
            How great Thou [Dm7]art, [G7]how great Thou [C]art!
            {eoc}

            {comment: Verse 2}
            When [C]through the woods and forest [F]glades I wander
            And [C]hear the birds sing [G7]sweetly in the [C]trees
            When I look down from lofty [F]mountain grandeur
            And [C]hear the brook and [G7]feel the gentle [C]breeze
        """.trimIndent()

        val EN_MANAM_ENGUM_CHORDPRO = """
            {title: En Manam Engum}
            {artist: Tamil Worship}
            {key: D}
            {tempo: 84}
            {time: 4/4}

            {comment: Verse 1}
            [D]என் மனம் எங்கும் [G]இயேசுவே
            [Em]உம் திரு நாமம் [A]வாழ்கவே
            [D]எந்தன் வாழ்வின் [G]வெளிச்சமே
            [A]என்றும் உம்மைப் [D]பாடுவேன்

            {comment: Chorus}
            {soc}
            [D]அல்லேலூயா [G]அல்லேலூயா
            [A]ஆண்டவர் இயேசுவுக்கு [D]அல்லேலூயா
            [Bm]துதியும் கனமும் [G]உமக்கே
            [Em]என்றென்றும் [A]உமக்கே [D]ஆமென்
            {eoc}

            {comment: Verse 2}
            [D]காரிருள் என்னைச் [G]சூழ்ந்தாலும்
            [Em]கால்கள் சறுக்கி [A]விழுந்தாலும்
            [D]கரம்பிடித்து [G]நடத்துவார்
            [A]கண்மணி போலக் [D]காப்பாரே
        """.trimIndent()

        val YESU_TERA_NAAM_CHORDPRO = """
            {title: Yesu Tera Naam}
            {artist: Hindi Worship}
            {key: E}
            {tempo: 80}
            {time: 4/4}

            {comment: Verse 1}
            [E]यीशु तेरा नाम [A]सबसे ऊँचा है
            [B]सारे जहाँ में [E]तू ही खुदा है
            [C#m]मुक्ति का दाता [A]तू ही हमारा
            [B]हर एक दिल का [E]तू ही सहारा

            {comment: Chorus}
            {soc}
            [E]गाओ हालेलुयाह [A]गाओ हालेलुयाह
            [B]प्रभु यीशु की जय [E]गाओ हालेलुयाह
            [C#m]राजाओं का राजा [A]प्रभुओं का प्रभु
            [B]सदा सर्वदा [E]तेरी स्तुति हो
            {eoc}
        """.trimIndent()

        val PREMINCHEDHAN_CHORDPRO = """
            {title: Preminchedhan}
            {artist: Telugu Worship}
            {key: G}
            {tempo: 76}
            {time: 4/4}

            {comment: Verse 1}
            [G]ప్రేమించెదన్ నిన్నే [C]యేసయ్యా
            [D]పూర్ణ హృదయముతో [G]నిన్నే
            [Em]సేవించెదన్ నిన్నే [C]దేవా
            [D]జీవిత కాలమంతా [G]నిన్నే

            {comment: Chorus}
            {soc}
            [G]హల్లెలూయా [C]హల్లెలూయా
            [D]హల్లెలూయా [G]యేసయ్యా
            [Em]హల్లెలూయా [C]హల్లెలూయా
            [D]ఆరాధన [G]నీకే
            {eoc}
        """.trimIndent()

        val STHUTHIKKU_PAATHRAN_CHORDPRO = """
            {title: Sthuthikku Paathran}
            {artist: Malayalam Worship}
            {key: A}
            {tempo: 82}
            {time: 4/4}

            {comment: Verse 1}
            [A]സ്തുതിക്കു പാത്രനായ [D]ദൈവമേ
            [E]നിൻ നാമം വാഴ്ത്തുന്നു [A]ഞങ്ങൾ
            [F#m]രക്ഷകനായ എൻ [D]യേശുവേ
            [E]നിന്നെ ഞാൻ ആരാധിക്കുന്നു [A]

            {comment: Chorus}
            {soc}
            [A]ഹല്ലേലൂയ്യാ [D]സ്തുതി ഗീതം
            [E]പാടുന്നു ഞങ്ങൾ [A]ആനന്ദത്താൽ
            [F#m]എന്നെന്നും നീ എൻ [D]സങ്കേതം
            [E]യേശുവേ നിനക്ക് [A]സ്തോത്രം
            {eoc}
        """.trimIndent()

        val NANNA_AATHMAVU_CHORDPRO = """
            {title: Nanna Aathmavu}
            {artist: Kannada Worship}
            {key: D}
            {tempo: 78}
            {time: 4/4}

            {comment: Verse 1}
            [D]ನನ್ನ ಆತ್ಮವು ನಿನ್ನನ್ನೇ [G]ಸ್ತುತಿಸುವುದು
            [A]ನನ್ನ ಹೃದಯವು ನಿನ್ನನ್ನೇ [D]ಆರಾಧಿಸುವುದು
            [Bm]ಯೇಸುವೇ ನೀನೇ ನನ್ನ [G]ಪಾಲಕನು
            [A]ಎಂದೆಂದಿಗೂ ನೀನೇ ನನ್ನ [D]ಆಶ್ರಯನು

            {comment: Chorus}
            {soc}
            [D]ಹಲ್ಲೇಲೂಯಾ [G]ಹಲ್ಲೇಲೂಯಾ
            [A]ಸ್ತೋತ್ರವು ನಿನಗೆ [D]ಯೇಸುವೇ
            [Bm]ಮಹಿಮೆಯು ನಿನಗೆ [G]ರಾಜನೇ
            [A]ಆರಾಧನೆ [D]ನಿನಗೆ
            {eoc}
        """.trimIndent()
    }
}
