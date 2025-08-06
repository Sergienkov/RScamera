package com.example.realsensecapture.ui

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.realsensecapture.rsnative.NativeBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

@Composable
fun PreviewScreen(modifier: Modifier = Modifier) {
    var rgbBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var depthBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { NativeBridge.startPreview() }
        while (isActive) {
            val rgb = withContext(Dispatchers.IO) { NativeBridge.getRgbFrame() }
            val depth = withContext(Dispatchers.IO) { NativeBridge.getDepthFrame() }
            rgb?.let { rgbBitmap = rgbToBitmap(it, 640, 480) }
            depth?.let { depthBitmap = depthToBitmap(it, 848, 480) }
            delay(16L)
        }
    }
    DisposableEffect(Unit) {
        onDispose { NativeBridge.stopPreview() }
    }

    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "RGB Preview",
            style = MaterialTheme.typography.titleMedium
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            rgbBitmap?.let { Image(bitmap = it, contentDescription = null) }
                ?: Text("RGB stream placeholder", color = Color.White)
        }

        Text(
            text = "Depth Preview",
            style = MaterialTheme.typography.titleMedium
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            depthBitmap?.let { Image(bitmap = it, contentDescription = null) }
                ?: Text("Depth stream placeholder", color = Color.White)
        }
    }
}

private fun rgbToBitmap(data: ByteArray, width: Int, height: Int): ImageBitmap {
    val pixels = IntArray(width * height)
    var i = 0
    for (p in 0 until width * height) {
        val r = data[i++].toInt() and 0xFF
        val g = data[i++].toInt() and 0xFF
        val b = data[i++].toInt() and 0xFF
        pixels[p] = -0x1000000 or (r shl 16) or (g shl 8) or b
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}

private fun depthToBitmap(data: ByteArray, width: Int, height: Int): ImageBitmap {
    val pixels = IntArray(width * height)
    var i = 0
    for (p in 0 until width * height) {
        val depth = (data[i + 1].toInt() and 0xFF shl 8) or (data[i].toInt() and 0xFF)
        val g = (depth / 32).coerceIn(0, 255)
        pixels[p] = -0x1000000 or (g shl 16) or (g shl 8) or g
        i += 2
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
        .asImageBitmap()
}

