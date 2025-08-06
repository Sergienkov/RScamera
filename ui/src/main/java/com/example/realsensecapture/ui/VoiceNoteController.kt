package com.example.realsensecapture.ui

import kotlinx.coroutines.flow.StateFlow
import java.io.File

interface VoiceNoteController {
    val level: StateFlow<Float>
    fun start(file: File)
    fun stop()
}
