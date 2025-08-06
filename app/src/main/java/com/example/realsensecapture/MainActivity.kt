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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import com.example.realsensecapture.ui.PreviewScreen
import com.example.realsensecapture.ui.SettingsRepository
import com.example.realsensecapture.rsnative.NativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settingsRepository = SettingsRepository(this)
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
                            val ok = withContext(Dispatchers.IO) {
                                NativeBridge.captureBurst(filesDir.absolutePath)
                            }
                            if (ok) {
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
