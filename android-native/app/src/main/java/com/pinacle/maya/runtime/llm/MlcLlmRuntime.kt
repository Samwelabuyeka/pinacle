package com.pinacle.maya.runtime.llm

import android.content.Context
import com.pinacle.maya.runtime.model.AssistantRuntimeProfile

class MlcLlmRuntime(
    private val context: Context,
    private val profile: AssistantRuntimeProfile
) : LanguageModelRuntime {
    private val binding = MlcAndroidBinding(context)
    private val reasoningEngine = OnDeviceReasoningEngine(profile)

    override fun generate(prompt: String): String {
        val status = binding.status()
        return reasoningEngine.respond(
            prompt = prompt,
            backendLabel = "MLC Android",
            heavyMode = false,
            backendReady = status.available,
            backendReason = status.reason
        )
    }
}
