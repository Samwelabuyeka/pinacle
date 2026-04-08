package com.pinacle.maya.offline

import android.content.Context
import com.pinacle.maya.memory.ConversationMemoryStore
import com.pinacle.maya.runtime.RuntimeProfileStore
import com.pinacle.maya.runtime.asr.SpeechRecognizerRuntime
import com.pinacle.maya.runtime.asr.WhisperCppSpeechRecognizer
import com.pinacle.maya.runtime.wake.OpenWakeWordDetector
import com.pinacle.maya.runtime.wake.WakeWordDetectorRuntime
import com.pinacle.maya.security.OwnerSessionStore
import com.pinacle.maya.security.PrivacyGuardian
import com.pinacle.maya.security.PrivacyPolicyEngine
import com.pinacle.maya.security.SecurityProfileStore

class AssistantOrchestrator(
    private val wakeWordDetector: WakeWordDetectorRuntime,
    private val speechRecognizer: SpeechRecognizerRuntime,
    private val voiceSessionController: VoiceSessionController
) {
    fun start() {
        wakeWordDetector.arm()
    }

    fun processAudio(audioChunk: ByteArray): String {
        val prompt = speechRecognizer.transcribe(audioChunk)
        val response = voiceSessionController.handlePrompt(prompt)
        return response
    }

    fun executeAction(intentName: String, arguments: Map<String, String> = emptyMap()): String {
        return voiceSessionController.handlePrompt(
            buildString {
                append(intentName)
                if (arguments.isNotEmpty()) {
                    append(" ")
                    append(arguments.entries.joinToString(" ") { "${it.key}=${it.value}" })
                }
            }
        )
    }

    companion object {
        fun create(context: Context, memoryStore: ConversationMemoryStore): AssistantOrchestrator {
            val profile = RuntimeProfileStore(context).load()
            val wakeWordDetector = OpenWakeWordDetector(profile)
            val speechRecognizer = WhisperCppSpeechRecognizer(profile)
            val deviceActionRouter = DeviceActionRouter(context)
            val inferenceCoordinator = OfflineInferenceCoordinator(context)
            val securityProfileStore = SecurityProfileStore(context)
            val ownerSessionStore = OwnerSessionStore(context)
            val privacyGuardian = PrivacyGuardian(context)
            val voiceSessionController = VoiceSessionController(
                inferenceCoordinator = inferenceCoordinator,
                deviceActionRouter = deviceActionRouter,
                memoryStore = memoryStore,
                privacyPolicyEngine = PrivacyPolicyEngine(securityProfileStore, ownerSessionStore),
                privacyGuardian = privacyGuardian
            )
            return AssistantOrchestrator(
                wakeWordDetector = wakeWordDetector,
                speechRecognizer = speechRecognizer,
                voiceSessionController = voiceSessionController
            )
        }
    }
}
