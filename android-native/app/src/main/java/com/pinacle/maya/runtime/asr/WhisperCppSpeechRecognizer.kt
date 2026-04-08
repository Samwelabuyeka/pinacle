package com.pinacle.maya.runtime.asr

import com.pinacle.maya.runtime.model.AssistantRuntimeProfile

class WhisperCppSpeechRecognizer(
    private val profile: AssistantRuntimeProfile
) : SpeechRecognizerRuntime {
    override fun transcribe(audioChunk: ByteArray): String {
        return if (audioChunk.isEmpty()) {
            "I did not receive any local audio yet. You can type to Maya now, and the whisper.cpp microphone bridge can be added as a packaged native module for full offline speech capture."
        } else {
            "Local voice input received for ${profile.assistantPersona}. This debug build captured audio bytes and is ready to hand them to the packaged ${profile.speechRecognitionBackend} runtime."
        }
    }
}
