package com.example.realsensecapture.data

import android.content.Context
import android.os.StatFs
import java.io.File
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.example.realsensecapture.rsnative.NativeBridge

class SessionRepository(
    private val context: Context,
    private val dao: SessionDao
) {
    /**
     * Creates a new capture session by invoking the native burst capture,
     * writing accompanying metadata and inserting a [SessionEntity] into Room.
     *
     * @param comment Optional text comment to be written into `meta.json`.
     * @return `true` if the capture and database insert succeeded.
     */
    suspend fun createSession(comment: String? = null): Boolean {
        val timestamp = Instant.now()
        val sessionDir = File(
            context.filesDir,
            "Captures/Session-${'$'}{timestamp.toString()}"
        ).apply { mkdirs() }

        val ok = withContext(Dispatchers.IO) {
            NativeBridge.captureBurst(sessionDir.absolutePath)
        }
        if (!ok) {
            sessionDir.deleteRecursively()
            return false
        }

        val rgbCount = sessionDir.listFiles { _, name ->
            name.startsWith("rgb_") && name.endsWith(".jpg")
        }?.size ?: 0

        val noteSrc = File(context.filesDir, "note.m4a")
        val hasNote = if (noteSrc.exists()) {
            noteSrc.renameTo(File(sessionDir, "note.m4a"))
        } else {
            false
        }

        val meta = JSONObject().apply {
            put("timestamp", timestamp.toString())
            put("rgbCount", rgbCount)
            put("hasNote", hasNote)
            comment?.let { put("comment", it) }
        }
        File(sessionDir, "meta.json").writeText(meta.toString())

        dao.insert(
            SessionEntity(
                folderPath = sessionDir.absolutePath,
                timestamp = timestamp.toEpochMilli(),
                rgbCount = rgbCount,
                hasNote = hasNote
            )
        )

        return true
    }

    suspend fun getAvailableSpaceBytes(): Long = withContext(Dispatchers.IO) {
        val statFs = StatFs(context.filesDir.absolutePath)
        statFs.availableBytes
    }

    suspend fun hasSufficientSpace(minFreeBytes: Long): Boolean =
        getAvailableSpaceBytes() >= minFreeBytes

    fun getAll() = dao.getAll()

    fun getById(id: Long) = dao.getById(id)

    suspend fun updateHasNote(id: Long, hasNote: Boolean) = dao.updateHasNote(id, hasNote)
}
