package com.example.realsensecapture.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val folderPath: String,
    val timestamp: Long,
    val rgbCount: Int,
    val hasNote: Boolean
)
