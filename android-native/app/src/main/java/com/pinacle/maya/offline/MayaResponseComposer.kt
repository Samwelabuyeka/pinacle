package com.pinacle.maya.offline

import com.pinacle.maya.memory.ConversationMemoryStore

class MayaResponseComposer(
    private val memoryStore: ConversationMemoryStore
) {
    fun compose(
        prompt: String,
        modelOutput: String,
        actionResults: List<String>,
        blurResponse: Boolean = false,
        privacyPolicyEngine: com.pinacle.maya.security.PrivacyPolicyEngine? = null
    ): String {
        val intro = when {
            actionResults.any { it.contains("opened", ignoreCase = true) } -> "I handled the device step and kept the rest local."
            actionResults.isNotEmpty() -> "I prepared the phone action and kept your request offline."
            else -> "I am working as Maya with your local model stack."
        }
        val context = memoryStore.lastTurnSummary().lineSequence().firstOrNull().orEmpty()
        val renderedOutput = if (blurResponse) {
            privacyPolicyEngine?.redact(modelOutput) ?: "Protected by Maya privacy shield."
        } else {
            modelOutput
        }
        return buildString {
            append(intro)
            if (context.isNotBlank()) {
                append(" ")
                append(context)
            }
            append("\n")
            append(renderedOutput)
        }
    }
}
