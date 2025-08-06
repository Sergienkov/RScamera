package com.example.realsensecapture.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.realsensecapture.data.AppDatabase
import com.example.realsensecapture.rsnative.NativeBridge
import com.example.realsensecapture.ui.VoiceNoteController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

@Composable
fun SessionDetailsScreen(
    sessionId: Long,
    controller: VoiceNoteController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).sessionDao() }
    val session by dao.getById(sessionId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()

    session?.let { s ->
        var index by remember { mutableStateOf(0) }
        var image by remember { mutableStateOf<ImageBitmap?>(null) }
        var comment by remember { mutableStateOf<String?>(null) }
        var playback by remember { mutableStateOf<ImageBitmap?>(null) }
        var recording by remember { mutableStateOf(false) }
        val level by controller.level.collectAsState()

        LaunchedEffect(index, s.folderPath) {
            image = loadImage(s.folderPath, index)
        }

        LaunchedEffect(s.folderPath) {
            comment = loadComment(s.folderPath)
        }

        LaunchedEffect(s.folderPath) {
            val bag = File(s.folderPath, "depth_0.1s.bag").absolutePath
            withContext(Dispatchers.IO) { NativeBridge.startPlayback(bag) }
            while (isActive) {
                val rgb = withContext(Dispatchers.IO) { NativeBridge.getRgbFrame() }
                rgb?.let { playback = rgbToBitmap(it, 640, 480) }
                delay(33L)
            }
        }
        DisposableEffect(s.folderPath) {
            onDispose { NativeBridge.stopPlayback() }
        }

        Column(modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                image?.let { Image(bitmap = it, contentDescription = null) }
                    ?: Text("No image", color = Color.White)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { if (index > 0) index-- }) { Text("Prev") }
                Text("${'$'}{index + 1} / ${'$'}{s.rgbCount}")
                Button(onClick = { if (index < s.rgbCount - 1) index++ }) { Text("Next") }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                playback?.let { Image(bitmap = it, contentDescription = null) }
                    ?: Text("Playback", color = Color.White)
            }
            comment?.let {
                Text(it, modifier = Modifier.padding(8.dp))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = {
                    if (!recording) {
                        controller.start(File(s.folderPath, "note.m4a"))
                        recording = true
                    } else {
                        controller.stop()
                        recording = false
                        scope.launch {
                            updateMetaNote(s.folderPath)
                            dao.updateHasNote(s.id, true)
                        }
                    }
                }) {
                    Text(if (recording) "Stop" else "Record")
                }
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .height(24.dp)
                        .fillMaxWidth()
                        .background(Color.Gray),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(level.coerceIn(0f, 1f))
                            .background(Color.Green)
                    )
                }
            }
        }
    } ?: Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Loading...")
    }
}

private suspend fun loadImage(folder: String, index: Int): ImageBitmap? =
    withContext(Dispatchers.IO) {
        val file = File(folder, String.format("rgb_%03d.jpg", index))
        if (!file.exists()) return@withContext null
        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
    }

private suspend fun loadComment(folder: String): String? =
    withContext(Dispatchers.IO) {
        val meta = File(folder, "meta.json")
        if (!meta.exists()) return@withContext null
        val obj = JSONObject(meta.readText())
        obj.optString("comment", null)
    }

private suspend fun updateMetaNote(folder: String) =
    withContext(Dispatchers.IO) {
        val meta = File(folder, "meta.json")
        val obj = if (meta.exists()) JSONObject(meta.readText()) else JSONObject()
        obj.put("hasNote", true)
        meta.writeText(obj.toString())
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

