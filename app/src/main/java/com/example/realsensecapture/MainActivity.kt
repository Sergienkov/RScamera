package com.example.realsensecapture

import android.os.Bundle
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
import com.example.realsensecapture.rsnative.NativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        scope.launch {
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
