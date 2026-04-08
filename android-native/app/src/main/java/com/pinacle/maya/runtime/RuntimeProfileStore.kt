package com.pinacle.maya.runtime

import android.content.Context
import com.pinacle.maya.runtime.model.AssistantRuntimeProfile
import com.pinacle.maya.runtime.model.LanguageModelBackend
import com.pinacle.maya.runtime.model.SpeechRecognitionBackend
import com.pinacle.maya.runtime.model.SpeechSynthesisBackend
import com.pinacle.maya.runtime.model.WakeWordBackend
import org.json.JSONObject

class RuntimeProfileStore(private val context: Context) {
    fun load(): AssistantRuntimeProfile {
        val raw = context.assets.open("runtime_profile.json").bufferedReader().use { it.readText() }
        val json = JSONObject(raw)
        return AssistantRuntimeProfile(
            languageModelBackend = LanguageModelBackend.valueOf(json.getString("languageModelBackend")),
            wakeWordBackend = WakeWordBackend.valueOf(json.getString("wakeWordBackend")),
            speechRecognitionBackend = SpeechRecognitionBackend.valueOf(json.getString("speechRecognitionBackend")),
            speechSynthesisBackend = SpeechSynthesisBackend.valueOf(json.getString("speechSynthesisBackend")),
            primaryModelId = json.getString("primaryModelId"),
            fallbackModelId = json.getString("fallbackModelId"),
            heavyModelId = json.optString("heavyModelId", json.getString("fallbackModelId")),
            phoneModelId = json.optString("phoneModelId", json.getString("primaryModelId")),
            modelRoutingStrategy = json.optString("modelRoutingStrategy", "phone_primary_heavy_escalation"),
            voiceProfileId = json.optString("voiceProfileId", "maya-premium-female-en"),
            voiceStyle = json.optString("voiceStyle", "warm_confident_expressive"),
            voiceBackendTarget = json.optString("voiceBackendTarget", "piper_high_quality_female"),
            phoneOptimized = json.getBoolean("phoneOptimized"),
            assistantPersona = json.getString("assistantPersona"),
            targetDeviceClass = json.getString("targetDeviceClass"),
            requiresPrivilegedAccess = json.optBoolean("requiresPrivilegedAccess", true)
        )
    }
}
