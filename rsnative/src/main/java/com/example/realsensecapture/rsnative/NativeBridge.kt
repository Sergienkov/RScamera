package com.example.realsensecapture.rsnative

object NativeBridge {
    init {
        System.loadLibrary("rsnative-lib")
    }

    external fun hello(): String
    external fun startPreview(): Boolean
    external fun stopPreview()
    external fun getRgbFrame(): ByteArray?
    external fun getDepthFrame(): ByteArray?
    external fun captureBurst(dirPath: String): Boolean
    external fun startPlayback(path: String): Boolean
    external fun stopPlayback()
}
