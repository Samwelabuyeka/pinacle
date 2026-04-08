package com.pinacle.maya.runtime.asr

interface SpeechRecognizerRuntime {
    fun transcribe(audioChunk: ByteArray): String
}
