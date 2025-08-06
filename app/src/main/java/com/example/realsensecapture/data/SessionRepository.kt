package com.example.realsensecapture.data

class SessionRepository(private val dao: SessionDao) {
    suspend fun insert(session: SessionEntity) = dao.insert(session)
}
