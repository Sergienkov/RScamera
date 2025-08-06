package com.example.realsensecapture.rsnative

object NativeBridge {
    init {
        System.loadLibrary("rsnative-lib")
    }

    external fun hello(): String
}
