package com.example.realsensecapture

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Bundle
import android.content.Context.RECEIVER_NOT_EXPORTED
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.example.realsensecapture.data.AppDatabase
import com.example.realsensecapture.data.SessionRepository
import com.example.realsensecapture.ui.RealSenseCaptureApp
import com.example.realsensecapture.ui.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {

    private lateinit var sessionRepository: SessionRepository
    private lateinit var voiceNoteController: VoiceNoteController
    private lateinit var settingsRepository: SettingsRepository
    private var usbPermissionAction: String? = null
    private val usbPermissionFlow = MutableStateFlow(true)
    private var usbReceiverRegistered = false

    private val usbPermissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == usbPermissionAction) {
                usbPermissionFlow.value = hasUsbPermission()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getInstance(applicationContext)
        sessionRepository = SessionRepository(applicationContext, database.sessionDao())
        voiceNoteController = VoiceNoteController(this)
        settingsRepository = SettingsRepository(applicationContext)

        usbPermissionAction = "${packageName}.USB_PERMISSION"
        registerUsbReceiver()
        usbPermissionFlow.value = hasUsbPermission()
        if (!usbPermissionFlow.value) {
            requestUsbPermission()
        }

        setContent {
            val requiredPermissions = remember { runtimePermissions() }
            var hasStandardPermissions by rememberSaveable {
                mutableStateOf(checkPermissions(requiredPermissions))
            }
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { result ->
                val granted = result.values.all { it }
                hasStandardPermissions = granted || checkPermissions(requiredPermissions)
            }
            val hasUsbPermission by usbPermissionFlow.collectAsState()

            LaunchedEffect(requiredPermissions) {
                if (!hasStandardPermissions) {
                    permissionLauncher.launch(requiredPermissions)
                }
            }

            val permissionsGranted = hasStandardPermissions && hasUsbPermission
            if (permissionsGranted) {
                val navController = rememberNavController()
                RealSenseCaptureApp(
                    sessionRepository = sessionRepository,
                    voiceNoteController = voiceNoteController,
                    settingsRepository = settingsRepository,
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                PermissionRequestScreen(
                    hasStandardPermissions = hasStandardPermissions,
                    hasUsbPermission = hasUsbPermission,
                    onRequestStandardPermissions = {
                        permissionLauncher.launch(requiredPermissions)
                    },
                    onRequestUsbPermission = { requestUsbPermission() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (usbReceiverRegistered) {
            unregisterReceiver(usbPermissionReceiver)
            usbReceiverRegistered = false
        }
        voiceNoteController.stop()
    }

    private fun runtimePermissions(): Array<String> {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.READ_MEDIA_IMAGES
            permissions += Manifest.permission.READ_MEDIA_VIDEO
        } else {
            permissions += Manifest.permission.READ_EXTERNAL_STORAGE
            permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
        }
        return permissions.distinct().toTypedArray()
    }

    private fun checkPermissions(permissions: Array<String>): Boolean =
        permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

    private fun requestUsbPermission() {
        val action = usbPermissionAction ?: return
        val manager = usbManager()
        val devices = manager.deviceList.values.filterNot { manager.hasPermission(it) }
        if (devices.isEmpty()) {
            usbPermissionFlow.value = true
            return
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(action),
            PendingIntent.FLAG_IMMUTABLE
        )
        devices.forEach { device ->
            manager.requestPermission(device, pendingIntent)
        }
    }

    private fun hasUsbPermission(): Boolean {
        val manager = usbManager()
        val devices = manager.deviceList.values
        if (devices.isEmpty()) return true
        return devices.all { manager.hasPermission(it) }
    }

    private fun registerUsbReceiver() {
        val action = usbPermissionAction ?: return
        val filter = IntentFilter(action)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(usbPermissionReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(usbPermissionReceiver, filter)
        }
        usbReceiverRegistered = true
    }

    private fun usbManager(): UsbManager = getSystemService(UsbManager::class.java)

    @Composable
    private fun PermissionRequestScreen(
        hasStandardPermissions: Boolean,
        hasUsbPermission: Boolean,
        onRequestStandardPermissions: () -> Unit,
        onRequestUsbPermission: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Permissions are required to use the camera features.")
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasStandardPermissions) {
                Button(onClick = onRequestStandardPermissions) {
                    Text("Grant microphone and storage access")
                }
            }
            if (!hasUsbPermission) {
                Button(
                    onClick = onRequestUsbPermission,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Grant USB access")
                }
            }
        }
    }
}
