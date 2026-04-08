package com.pinacle.maya.offline

import android.content.Context
import com.pinacle.maya.runtime.tts.PiperSpeechSynthesizer
import com.pinacle.maya.runtime.RuntimeProfileStore
import com.pinacle.maya.runtime.llm.BitNetRuntime
import com.pinacle.maya.runtime.llm.LanguageModelRuntime
import com.pinacle.maya.runtime.llm.MlcLlmRuntime
import com.pinacle.maya.runtime.model.AssistantRuntimeProfile
import com.pinacle.maya.runtime.model.LanguageModelBackend

class OfflineInferenceCoordinator(private val context: Context) {
    private val runtimeProfile: AssistantRuntimeProfile = RuntimeProfileStore(context).load()
    private val speechSynthesizer = PiperSpeechSynthesizer(context, runtimeProfile)
    private val primaryRuntime: LanguageModelRuntime = when (runtimeProfile.languageModelBackend) {
        LanguageModelBackend.MLC -> MlcLlmRuntime(context, runtimeProfile)
        LanguageModelBackend.BITNET -> BitNetRuntime(runtimeProfile)
    }
    private val heavyRuntime: LanguageModelRuntime = BitNetRuntime(runtimeProfile)

    fun transcribe(audioChunk: ByteArray): String {
        return "offline_transcription_pending"
    }

    fun generateResponse(prompt: String, preferHeavyModel: Boolean = false): String {
        return if (preferHeavyModel) {
            heavyRuntime.generate(prompt)
        } else {
            primaryRuntime.generate(prompt)
        }
    }

    fun synthesizeSpeech(text: String) {
        speechSynthesizer.speak(text)
    }

    fun activeVoiceDescription(): String {
        return speechSynthesizer.describeVoice()
    }
}
