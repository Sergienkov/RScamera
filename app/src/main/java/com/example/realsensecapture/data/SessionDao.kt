package com.example.realsensecapture.data

import androidx.room.Dao
import androidx.room.Insert

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity)
}
