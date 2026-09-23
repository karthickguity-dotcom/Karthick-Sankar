package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SetlistEntity
import com.example.data.model.SetlistSongEntity
import com.example.data.model.SetlistSongWithDetails
import com.example.data.model.SetlistSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface SetlistDao {

    @Query("""
        SELECT s.id, s.name, s.description, s.eventDate, s.createdAt, s.updatedAt,
               COUNT(ss.id) AS songCount
        FROM setlists s
        LEFT JOIN setlist_songs ss ON s.id = ss.setlistId
        GROUP BY s.id
        ORDER BY s.updatedAt DESC
    """)
    fun getAllSetlistSummaries(): Flow<List<SetlistSummary>>

    @Query("SELECT * FROM setlists WHERE id = :id LIMIT 1")
    fun getSetlistById(id: Long): Flow<SetlistEntity?>

    @Query("SELECT * FROM setlists WHERE id = :id LIMIT 1")
    suspend fun getSetlistByIdDirect(id: Long): SetlistEntity?

    @Query("""
        SELECT 
            ss.id AS itemId,
            ss.setlistId AS setlistId,
            ss.songId AS songId,
            ss.orderIndex AS orderIndex,
            ss.customKey AS customKey,
            ss.performanceNotes AS performanceNotes,
            ss.targetDurationSeconds AS targetDurationSeconds,
            s.title AS title,
            s.artist AS artist,
            s.originalKey AS originalKey,
            s.capo AS capo,
            s.tempo AS tempo,
            s.timeSignature AS timeSignature,
            s.rawChordPro AS rawChordPro
        FROM setlist_songs ss
        INNER JOIN songs s ON ss.songId = s.id
        WHERE ss.setlistId = :setlistId
        ORDER BY ss.orderIndex ASC
    """)
    fun getSetlistSongsWithDetails(setlistId: Long): Flow<List<SetlistSongWithDetails>>

    @Query("""
        SELECT 
            ss.id AS itemId,
            ss.setlistId AS setlistId,
            ss.songId AS songId,
            ss.orderIndex AS orderIndex,
            ss.customKey AS customKey,
            ss.performanceNotes AS performanceNotes,
            ss.targetDurationSeconds AS targetDurationSeconds,
            s.title AS title,
            s.artist AS artist,
            s.originalKey AS originalKey,
            s.capo AS capo,
            s.tempo AS tempo,
            s.timeSignature AS timeSignature,
            s.rawChordPro AS rawChordPro
        FROM setlist_songs ss
        INNER JOIN songs s ON ss.songId = s.id
        WHERE ss.setlistId = :setlistId
        ORDER BY ss.orderIndex ASC
    """)
    suspend fun getSetlistSongsWithDetailsDirect(setlistId: Long): List<SetlistSongWithDetails>

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM setlist_songs WHERE setlistId = :setlistId")
    suspend fun getMaxOrderIndex(setlistId: Long): Int

    @Query("SELECT COUNT(*) FROM setlists")
    suspend fun getSetlistCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlist(setlist: SetlistEntity): Long

    @Update
    suspend fun updateSetlist(setlist: SetlistEntity)

    @Query("DELETE FROM setlists WHERE id = :id")
    suspend fun deleteSetlistById(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSong(item: SetlistSongEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetlistSongs(items: List<SetlistSongEntity>)

    @Query("DELETE FROM setlist_songs WHERE id = :itemId")
    suspend fun deleteSetlistSongById(itemId: Long)

    @Query("DELETE FROM setlist_songs WHERE setlistId = :setlistId AND songId = :songId")
    suspend fun removeSongFromSetlist(setlistId: Long, songId: Long)

    @Query("UPDATE setlist_songs SET orderIndex = :newOrder WHERE id = :itemId")
    suspend fun updateSongOrder(itemId: Long, newOrder: Int)

    @Query("UPDATE setlist_songs SET customKey = :key WHERE id = :itemId")
    suspend fun updateCustomKey(itemId: Long, key: String)

    @Query("UPDATE setlist_songs SET performanceNotes = :notes WHERE id = :itemId")
    suspend fun updatePerformanceNotes(itemId: Long, notes: String)

    @Query("UPDATE setlists SET updatedAt = :timestamp WHERE id = :id")
    suspend fun touchSetlist(id: Long, timestamp: Long = System.currentTimeMillis())
}
