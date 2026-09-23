package com.example

import com.example.data.model.SetlistEntity
import com.example.data.model.SetlistSongEntity
import com.example.data.model.SetlistSongWithDetails
import com.example.data.model.SetlistWithSongs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SetlistTest {

    @Test
    fun testSetlistEntityCreation() {
        val setlist = SetlistEntity(
            id = 1L,
            name = "Sunday Morning Service",
            description = "Main worship set with acoustic transition"
        )
        assertEquals(1L, setlist.id)
        assertEquals("Sunday Morning Service", setlist.name)
        assertEquals("Main worship set with acoustic transition", setlist.description)
        assertTrue(setlist.createdAt > 0)
    }

    @Test
    fun testSetlistSongOrderingAndDetails() {
        val song1 = SetlistSongWithDetails(
            itemId = 101L,
            setlistId = 1L,
            songId = 10L,
            orderIndex = 0,
            customKey = "D",
            performanceNotes = "Acoustic fingerpicking intro",
            targetDurationSeconds = 240,
            title = "Amazing Grace",
            artist = "John Newton",
            originalKey = "G",
            capo = 2,
            tempo = "75",
            timeSignature = "3/4",
            rawChordPro = "{title: Amazing Grace}\n[G]Amazing [C]grace"
        )

        val song2 = SetlistSongWithDetails(
            itemId = 102L,
            setlistId = 1L,
            songId = 20L,
            orderIndex = 1,
            customKey = "E",
            performanceNotes = "Full band swells into chorus",
            targetDurationSeconds = 300,
            title = "10,000 Reasons",
            artist = "Matt Redman",
            originalKey = "E",
            capo = 0,
            tempo = "72",
            timeSignature = "4/4",
            rawChordPro = "{title: 10,000 Reasons}\n[E]Bless the [B]Lord"
        )

        val setlistWithSongs = SetlistWithSongs(
            setlist = SetlistEntity(id = 1L, name = "Live Concert"),
            songs = listOf(song1, song2)
        )

        assertEquals(2, setlistWithSongs.songs.size)
        assertEquals("Amazing Grace", setlistWithSongs.songs[0].title)
        assertEquals("D", setlistWithSongs.songs[0].customKey)
        assertEquals("10,000 Reasons", setlistWithSongs.songs[1].title)
        assertEquals("Full band swells into chorus", setlistWithSongs.songs[1].performanceNotes)
    }

    @Test
    fun testSetlistSongReordering() {
        val items = mutableListOf("Song A", "Song B", "Song C")
        
        // Move Song B down
        val item = items.removeAt(1)
        items.add(2, item)
        assertEquals(listOf("Song A", "Song C", "Song B"), items)

        // Move Song C up
        val itemC = items.removeAt(1)
        items.add(0, itemC)
        assertEquals(listOf("Song C", "Song A", "Song B"), items)
    }

    @Test
    fun testLiveNavigationBoundaries() {
        val totalSongs = 3
        var activeIndex = 0

        fun hasNext() = activeIndex < totalSongs - 1
        fun hasPrev() = activeIndex > 0

        // At index 0: no prev, has next
        assertFalse(hasPrev())
        assertTrue(hasNext())

        // Move next
        activeIndex++
        assertTrue(hasPrev())
        assertTrue(hasNext())

        // Move next to end
        activeIndex++
        assertTrue(hasPrev())
        assertFalse(hasNext())
    }
}
