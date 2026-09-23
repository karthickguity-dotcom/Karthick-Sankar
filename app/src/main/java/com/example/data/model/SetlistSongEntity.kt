package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "setlist_songs",
    foreignKeys = [
        ForeignKey(
            entity = SetlistEntity::class,
            parentColumns = ["id"],
            childColumns = ["setlistId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("setlistId"),
        Index("songId")
    ]
)
data class SetlistSongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val setlistId: Long,
    val songId: Long,
    val orderIndex: Int,
    val customKey: String = "",
    val performanceNotes: String = "",
    val targetDurationSeconds: Int = 0
)
