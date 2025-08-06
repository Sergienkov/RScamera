package com.example.realsensecapture

import android.content.Context
import android.media.MediaRecorder
import com.example.realsensecapture.ui.VoiceNoteController as UiVoiceNoteController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class VoiceNoteController(private val context: Context) : UiVoiceNoteController {
    private var recorder: MediaRecorder? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var levelJob: Job? = null
    private val _level = MutableStateFlow(0f)
    override val level: StateFlow<Float> = _level

    override fun start(file: File) {
        stop()
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(96_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }
        levelJob = scope.launch {
            while (isActive) {
                val amp = recorder?.maxAmplitude ?: 0
                _level.value = amp / 32767f
                delay(100L)
            }
        }
    }

    override fun stop() {
        levelJob?.cancel()
        levelJob = null
        recorder?.apply {
            try {
                stop()
            } catch (_: Exception) {
            }
            release()
        }
        recorder = null
        _level.value = 0f
    }
}
