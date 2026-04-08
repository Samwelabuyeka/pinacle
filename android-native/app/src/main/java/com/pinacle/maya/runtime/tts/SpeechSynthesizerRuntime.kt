package com.pinacle.maya.runtime.tts

interface SpeechSynthesizerRuntime {
    fun speak(text: String)
    fun describeVoice(): String
}
