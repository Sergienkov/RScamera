package com.example.realsensecapture.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.realsensecapture.data.SessionRepository
import com.example.realsensecapture.rsnative.NativeBridge
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun PreviewScreen(
    sessionRepository: SessionRepository,
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    onNavigateToGallery: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onCaptureSuccess: () -> Unit = onNavigateToGallery
) {
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val freeSpaceThreshold by settingsRepository.thresholdFlow
        .collectAsState(initial = SettingsRepository.DEFAULT_THRESHOLD_BYTES)

    Box(modifier = modifier.fillMaxSize()) {
        PreviewSurface(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = onNavigateToSettings) {
                Text("Settings")
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = {
                    if (isCapturing) return@Button
                    scope.launch {
                        isCapturing = true
                        errorMessage = null

                        val freeBytes = try {
                            sessionRepository.getAvailableSpaceBytes()
                        } catch (t: Throwable) {
                            if (t is CancellationException) throw t
                            errorMessage = t.message ?: "Unable to check free space"
                            isCapturing = false
                            return@launch
                        }

                        if (freeBytes < freeSpaceThreshold) {
                            val requiredMb = freeSpaceThreshold / (1024 * 1024)
                            val availableMb = freeBytes / (1024 * 1024)
                            errorMessage =
                                "Not enough free space (required ≥ ${'$'}requiredMb MB, available ${'$'}availableMb MB)"
                            isCapturing = false
                            return@launch
                        }

                        var streamingStopped = false
                        val success = try {
                            NativeBridge.stopStreaming()
                            streamingStopped = true
                            sessionRepository.createSession()
                        } catch (t: Throwable) {
                            if (t is CancellationException) throw t
                            errorMessage = t.message ?: "Failed to start capture"
                            false
                        } finally {
                            if (streamingStopped) {
                                NativeBridge.startStreaming()
                            }
                        }

                        isCapturing = false

                        if (success) {
                            onCaptureSuccess()
                        } else if (errorMessage == null) {
                            errorMessage = "Failed to create session"
                        }
                    }
                },
                enabled = !isCapturing
            ) {
                if (isCapturing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Capturing…")
                    }
                } else {
                    Text("Start Capture")
                }
            }

            FloatingActionButton(
                onClick = onNavigateToGallery
            ) {
                Text("Gallery")
            }
        }
    }
}
