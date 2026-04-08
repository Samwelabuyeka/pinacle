package com.pinacle.maya.runtime.model

data class AssistantRuntimeProfile(
    val languageModelBackend: LanguageModelBackend,
    val wakeWordBackend: WakeWordBackend,
    val speechRecognitionBackend: SpeechRecognitionBackend,
    val speechSynthesisBackend: SpeechSynthesisBackend,
    val primaryModelId: String,
    val fallbackModelId: String,
    val heavyModelId: String,
    val phoneModelId: String,
    val modelRoutingStrategy: String,
    val voiceProfileId: String,
    val voiceStyle: String,
    val voiceBackendTarget: String,
    val phoneOptimized: Boolean,
    val assistantPersona: String,
    val targetDeviceClass: String,
    val requiresPrivilegedAccess: Boolean
) {
    fun describe(): String {
        return buildString {
            appendLine("Assistant persona: $assistantPersona")
            appendLine("LLM backend: $languageModelBackend")
            appendLine("Wake word: $wakeWordBackend")
            appendLine("ASR: $speechRecognitionBackend")
            appendLine("TTS: $speechSynthesisBackend")
            appendLine("Primary model: $primaryModelId")
            appendLine("Fallback model: $fallbackModelId")
            appendLine("Heavy model: $heavyModelId")
            appendLine("Phone model: $phoneModelId")
            appendLine("Routing: $modelRoutingStrategy")
            appendLine("Voice profile: $voiceProfileId")
            appendLine("Voice style: $voiceStyle")
            appendLine("Voice backend target: $voiceBackendTarget")
            appendLine("Target device class: $targetDeviceClass")
            append("Privileged access required: $requiresPrivilegedAccess")
        }
    }
}
