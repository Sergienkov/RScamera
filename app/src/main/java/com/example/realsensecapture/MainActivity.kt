package com.example.realsensecapture

import android.os.Bundle
import android.os.StatFs
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.example.realsensecapture.ui.PreviewScreen
import com.example.realsensecapture.ui.SettingsRepository
import com.example.realsensecapture.ui.SettingsScreen
import com.example.realsensecapture.data.SessionRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

import com.example.realsensecapture.data.AppDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)
        val sessionDao = AppDatabase.getInstance(this).sessionDao()
        val sessionRepository = SessionRepository(this, sessionDao)
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var showSettings by remember { mutableStateOf(false) }
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = { Text(if (showSettings) "Settings" else "Preview") },
                        actions = {
                            TextButton(onClick = { showSettings = !showSettings }) {
                                Text(if (showSettings) "Preview" else "Settings")

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
 main
                            }
                        }
                    )
                },
                floatingActionButton = {
                    if (!showSettings) {
                        FloatingActionButton(onClick = {
                            scope.launch {
                                val threshold = settingsRepository.thresholdFlow.first()
                                val statFs = StatFs(filesDir.absolutePath)
                                val available = statFs.availableBytes
                                if (available < threshold) {
                                    snackbarHostState.showSnackbar("Not enough space")
                                    return@launch
                                }
                                val ok = sessionRepository.createSession()
                                if (ok) {
                                    snackbarHostState.showSnackbar("Saved")
                                }
                            }
                        }) {
                            Text("Capture")
                        }
                    }
                }
            ) { padding ->
                if (showSettings) {
                    SettingsScreen(modifier = Modifier.padding(padding))
                } else {
                    PreviewScreen(modifier = Modifier.padding(padding))
                }
            }
        }
    }
}
