package com.example.realsensecapture.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    fun getById(id: Long): Flow<SessionEntity?>

    @Query("UPDATE sessions SET hasNote = :hasNote WHERE id = :id")
    suspend fun updateHasNote(id: Long, hasNote: Boolean)
}
