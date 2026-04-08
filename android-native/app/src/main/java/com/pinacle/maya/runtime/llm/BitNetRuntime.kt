package com.pinacle.maya.runtime.llm

import com.pinacle.maya.runtime.model.AssistantRuntimeProfile

class BitNetRuntime(
    private val profile: AssistantRuntimeProfile
) : LanguageModelRuntime {
    private val reasoningEngine = OnDeviceReasoningEngine(profile)

    override fun generate(prompt: String): String {
        return reasoningEngine.respond(
            prompt = prompt,
            backendLabel = "BitNet",
            heavyMode = true,
            backendReady = true
        )
    }
}
