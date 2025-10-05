package com.example.realsensecapture.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
) {
    val scope = rememberCoroutineScope()

    val resolution by settingsRepository.resolutionFlow.collectAsState(initial = "848x480")
    val fps by settingsRepository.fpsFlow.collectAsState(initial = 60)
    val threshold by settingsRepository.thresholdFlow
        .collectAsState(initial = SettingsRepository.DEFAULT_THRESHOLD_BYTES)

    Column(modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onNavigateBack) { Text("Back") }
        OutlinedTextField(
            value = resolution,
            onValueChange = { new -> scope.launch { settingsRepository.setResolution(new) } },
            label = { Text("Resolution") }
        )
        OutlinedTextField(
            value = fps.toString(),
            onValueChange = { new ->
                new.toIntOrNull()?.let { value ->
                    scope.launch { settingsRepository.setFps(value) }
                }
            },
            label = { Text("FPS") }
        )
        OutlinedTextField(
            value = threshold.toString(),
            onValueChange = { new ->
                new.toLongOrNull()?.let { value ->
                    scope.launch { settingsRepository.setThreshold(value) }
                }
            },
            label = { Text("Free space threshold (bytes)") }
        )
    }
}
