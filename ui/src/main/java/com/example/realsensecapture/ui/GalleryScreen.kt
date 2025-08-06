package com.example.realsensecapture.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.realsensecapture.data.AppDatabase
import com.example.realsensecapture.data.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun GalleryScreen(
    modifier: Modifier = Modifier,
    onSessionClick: (SessionEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getInstance(context).sessionDao() }
    val sessions by dao.getAll().collectAsState(initial = emptyList())

    LazyColumn(modifier) {
        items(sessions) { session ->
            SessionItem(session = session, onClick = { onSessionClick(session) })
        }
    }
}

@Composable
private fun SessionItem(session: SessionEntity, onClick: () -> Unit) {
    var preview by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(session.folderPath) {
        preview = loadPreview(session)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        preview?.let {
            Image(bitmap = it, contentDescription = null, modifier = Modifier.size(120.dp))
        }
        Column(Modifier.padding(start = 8.dp)) {
            val formatter = remember {
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())
            }
            Text(formatter.format(Instant.ofEpochMilli(session.timestamp)))
            if (session.hasNote) {
                Text("Has note")
            }
        }
    }
}

private suspend fun loadPreview(session: SessionEntity): ImageBitmap? =
    withContext(Dispatchers.IO) {
        val dir = File(session.folderPath)
        val file = dir.listFiles { _, name -> name.startsWith("rgb_") && name.endsWith(".jpg") }
            ?.sorted()?.firstOrNull() ?: return@withContext null
        BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
    }

