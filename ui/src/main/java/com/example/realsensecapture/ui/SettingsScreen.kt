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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { SettingsRepository(context) }
    val scope = rememberCoroutineScope()

    val resolution by repository.resolutionFlow.collectAsState(initial = "848x480")
    val fps by repository.fpsFlow.collectAsState(initial = 60)
    val threshold by repository.thresholdFlow.collectAsState(initial = 100L * 1024 * 1024)

    Column(modifier.fillMaxSize().padding(16.dp)) {
        TextButton(onClick = onNavigateBack) { Text("Back") }
        OutlinedTextField(
            value = resolution,
            onValueChange = { new -> scope.launch { repository.setResolution(new) } },
            label = { Text("Resolution") }
        )
        OutlinedTextField(
            value = fps.toString(),
            onValueChange = { new -> new.toIntOrNull()?.let { scope.launch { repository.setFps(it) } } },
            label = { Text("FPS") }
        )
        OutlinedTextField(
            value = threshold.toString(),
            onValueChange = { new -> new.toLongOrNull()?.let { scope.launch { repository.setThreshold(it) } } },
            label = { Text("Free space threshold (bytes)") }
        )
    }
}

