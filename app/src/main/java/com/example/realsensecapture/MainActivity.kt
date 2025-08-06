package com.example.realsensecapture

import android.os.Bundle
import android.os.StatFs
import java.io.File
import java.time.Instant
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.example.realsensecapture.ui.PreviewScreen
import com.example.realsensecapture.ui.SettingsRepository
import com.example.realsensecapture.rsnative.NativeBridge
import com.example.realsensecapture.data.AppDatabase
import com.example.realsensecapture.data.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)
        val db = AppDatabase.getInstance(this)
        val sessionDao = db.sessionDao()
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        scope.launch {
                            val threshold = settingsRepository.thresholdFlow.first()
                            val statFs = StatFs(filesDir.absolutePath)
                            val available = statFs.availableBytes
                            if (available < threshold) {
                                snackbarHostState.showSnackbar("Not enough space")
                                return@launch
                            }
                            val timestamp = Instant.now()
                            val sessionDir = File(filesDir, "Captures/Session-${timestamp.toString()}").apply { mkdirs() }
                            val ok = withContext(Dispatchers.IO) {
                                NativeBridge.recordBurst(sessionDir.absolutePath)
                            }
                            if (ok) {
                                val rgbCount = sessionDir.listFiles { _, name ->
                                    name.startsWith("rgb_") && name.endsWith(".jpg")
                                }?.size ?: 0
                                val noteSrc = File(filesDir, "note.m4a")
                                val hasNote = if (noteSrc.exists()) {
                                    noteSrc.renameTo(File(sessionDir, "note.m4a"))
                                } else {
                                    false
                                }
                                val timestampIso = timestamp.toString()
                                val meta = JSONObject().apply {
                                    put("timestamp", timestampIso)
                                    put("rgbCount", rgbCount)
                                    put("hasNote", hasNote)
                                }
                                File(sessionDir, "meta.json").writeText(meta.toString())
                                withContext(Dispatchers.IO) {
                                    sessionDao.insert(
                                        SessionEntity(
                                            folderPath = sessionDir.absolutePath,
                                            timestamp = timestamp.toEpochMilli(),
                                            rgbCount = rgbCount,
                                            hasNote = hasNote
                                        )
                                    )
                                }
                                snackbarHostState.showSnackbar("Saved")
                            }
                        }
                    }) {
                        Text("Capture")
                    }
                }
            ) { padding ->
                PreviewScreen(modifier = Modifier.padding(padding))
            }
        }
    }
}
