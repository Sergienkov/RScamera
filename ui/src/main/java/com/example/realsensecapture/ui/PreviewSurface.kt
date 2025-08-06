package com.example.realsensecapture.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.hardware.usb.UsbManager
import android.view.SurfaceView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.realsensecapture.rsnative.NativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun PreviewSurface(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val surfaceView = remember { SurfaceView(context) }

    LaunchedEffect(surfaceView) {
        withContext(Dispatchers.IO) {
            NativeBridge.startStreaming()
            val width = 1280
            val height = 480
            while (isActive) {
                val frame = NativeBridge.getCombinedFrame()
                frame?.let {
                    val bmp = frameToBitmap(it, width, height)
                    val canvas = surfaceView.holder.lockCanvas()
                    canvas.drawBitmap(bmp, 0f, 0f, null)
                    surfaceView.holder.unlockCanvasAndPost(canvas)
                }
                delay(16L)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                NativeBridge.startStreaming()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                NativeBridge.stopStreaming()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        val usbReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> NativeBridge.startStreaming()
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> NativeBridge.stopStreaming()
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        context.registerReceiver(usbReceiver, filter)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            context.unregisterReceiver(usbReceiver)
            NativeBridge.stopStreaming()
        }
    }

    AndroidView(
        factory = { surfaceView },
        modifier = modifier
    )
}

private fun frameToBitmap(data: ByteArray, width: Int, height: Int): Bitmap {
    val pixels = IntArray(width * height)
    var i = 0
    for (p in pixels.indices) {
        val r = data[i++].toInt() and 0xFF
        val g = data[i++].toInt() and 0xFF
        val b = data[i++].toInt() and 0xFF
        pixels[p] = -0x1000000 or (r shl 16) or (g shl 8) or b
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
}
