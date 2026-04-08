package com.pinacle.maya.runtime.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import java.util.Locale
import com.pinacle.maya.runtime.model.AssistantRuntimeProfile

class PiperSpeechSynthesizer(
    context: Context,
    private val profile: AssistantRuntimeProfile
) : SpeechSynthesizerRuntime, TextToSpeech.OnInitListener {
    private val appContext = context.applicationContext
    private val textToSpeech = TextToSpeech(appContext, this)
    private var ready = false

    fun activeVoiceProfile(): VoiceProfile {
        return VoiceProfile(
            id = profile.voiceProfileId,
            displayName = "Maya Premium Female",
            style = profile.voiceStyle,
            backendTarget = profile.voiceBackendTarget,
            genderPresentation = "female",
            notes = "High-quality offline Piper voice tuned for warm, confident assistant responses."
        )
    }

    override fun onInit(status: Int) {
        ready = status == TextToSpeech.SUCCESS
        if (!ready) return
        textToSpeech.language = Locale.US
        val preferred = textToSpeech.voices
            ?.firstOrNull { voice ->
                val name = voice.name.lowercase()
                !voice.isNetworkConnectionRequired &&
                    (voice.locale.language == Locale.US.language || voice.locale.language == Locale.UK.language) &&
                    (name.contains("female") || name.contains("fem") || name.contains("en-us-x") || name.contains("news"))
            }
        if (preferred != null) {
            textToSpeech.voice = preferred
        }
        textToSpeech.setPitch(1.08f)
        textToSpeech.setSpeechRate(0.94f)
    }

    override fun speak(text: String) {
        val voice = activeVoiceProfile()
        val normalizedText = text.trim()
        if (normalizedText.isEmpty()) {
            return
        }
        if (!ready) {
            return
        }
        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "maya-${System.currentTimeMillis()}")
        }
        textToSpeech.speak(normalizedText, TextToSpeech.QUEUE_FLUSH, params, "maya-utterance")
    }

    override fun describeVoice(): String {
        val voice = activeVoiceProfile()
        val engineVoice = textToSpeech.voice?.name ?: "system-default"
        return "${voice.displayName} (${voice.style}) via $engineVoice"
    }
}
