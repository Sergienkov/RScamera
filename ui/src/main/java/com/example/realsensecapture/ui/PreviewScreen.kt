package com.example.realsensecapture.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PreviewScreen(
    modifier: Modifier = Modifier,
    onNavigateToGallery: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        PreviewSurface(modifier = Modifier.matchParentSize())

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

        FloatingActionButton(
            onClick = onNavigateToGallery,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Gallery")
        }
    }
}
