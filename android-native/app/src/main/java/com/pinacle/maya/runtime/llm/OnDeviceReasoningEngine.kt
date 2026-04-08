package com.pinacle.maya.runtime.llm

import com.pinacle.maya.runtime.model.AssistantRuntimeProfile

class OnDeviceReasoningEngine(
    private val profile: AssistantRuntimeProfile
) {
    fun respond(
        prompt: String,
        backendLabel: String,
        heavyMode: Boolean,
        backendReady: Boolean,
        backendReason: String? = null
    ): String {
        val cleanedPrompt = prompt.trim()
        if (cleanedPrompt.isBlank()) {
            return "I am ready. Ask me to plan, launch something, draft a message, or manage a reminder locally on your phone."
        }
        val normalized = cleanedPrompt.lowercase()
        val backendSummary = buildBackendSummary(backendLabel, heavyMode, backendReady, backendReason)
        val intentResponse = when {
            isCapabilityQuestion(normalized) -> capabilityResponse()
            isPlanningQuestion(normalized) -> planningResponse(cleanedPrompt)
            isComparisonQuestion(normalized) -> comparisonResponse(cleanedPrompt)
            isReminderPrompt(normalized) -> reminderResponse(cleanedPrompt)
            isCommunicationPrompt(normalized) -> communicationResponse(cleanedPrompt)
            isNavigationPrompt(normalized) -> navigationResponse(cleanedPrompt)
            isResearchPrompt(normalized) -> researchResponse(cleanedPrompt)
            else -> generalResponse(cleanedPrompt, heavyMode)
        }
        return "$intentResponse\n$backendSummary"
    }

    private fun capabilityResponse(): String {
        return buildString {
            append("I can operate as a local Android assistant with memory, reminders, alarms, calls, messaging drafts, app launch, web handoff, maps, notifications, and settings control. ")
            append("For deeper Siri-class control, Maya still depends on the phone granting notification listener, accessibility, dialer or SMS roles, and any OEM bridge that HyperOS exposes.")
        }
    }

    private fun planningResponse(prompt: String): String {
        return buildString {
            append("Here is the local Maya approach for that request: ")
            append(summarizePrompt(prompt))
            append(". I would break it into goal capture, action routing, confirmation, execution, and a spoken recap so the assistant stays reliable offline.")
        }
    }

    private fun comparisonResponse(prompt: String): String {
        return buildString {
            append("For ")
            append(summarizePrompt(prompt))
            append(", Maya should prioritize local privacy, fast action execution, and memory continuity over flashy cloud-only replies. ")
            append("That makes the experience feel more dependable than a generic chatbot even before the heaviest model is active.")
        }
    }

    private fun reminderResponse(prompt: String): String {
        return buildString {
            append("I understood a reminder or task request from: ")
            append(summarizePrompt(prompt))
            append(". Maya can prepare the native reminder flow and keep the context in local memory so follow-up requests stay coherent.")
        }
    }

    private fun communicationResponse(prompt: String): String {
        return buildString {
            append("I recognized a communication request in: ")
            append(summarizePrompt(prompt))
            append(". Maya can draft the call, SMS, or email path locally, then hand it to Android with the right app or role.")
        }
    }

    private fun navigationResponse(prompt: String): String {
        return buildString {
            append("I picked up a navigation or launch request from: ")
            append(summarizePrompt(prompt))
            append(". Maya can route that through apps, maps, settings, or the browser without sending the whole session to the cloud.")
        }
    }

    private fun researchResponse(prompt: String): String {
        return buildString {
            append("For ")
            append(summarizePrompt(prompt))
            append(", Maya should search, gather, and summarize in stages. On phone, the fast model handles intent and routing first, while the deeper reasoning tier is reserved for longer synthesis.")
        }
    }

    private fun generalResponse(prompt: String, heavyMode: Boolean): String {
        return buildString {
            append("I understood: ")
            append(summarizePrompt(prompt))
            append(". Maya is responding with a ")
            append(if (heavyMode) "deeper reasoning" else "phone-first")
            append(" local assistant path tuned for offline use, native Android actions, and a warm female voice.")
        }
    }

    private fun buildBackendSummary(
        backendLabel: String,
        heavyMode: Boolean,
        backendReady: Boolean,
        backendReason: String?
    ): String {
        val mode = if (heavyMode) "heavy reasoning" else "phone reasoning"
        return if (backendReady) {
            "$backendLabel is active for $mode using ${if (heavyMode) profile.heavyModelId else profile.phoneModelId}."
        } else {
            buildString {
                append("Maya is using the built-in local reasoning core while ")
                append(backendLabel)
                append(" is still being finalized")
                backendReason?.takeIf { it.isNotBlank() }?.let {
                    append(" (")
                    append(it)
                    append(")")
                }
                append(".")
            }
        }
    }

    private fun summarizePrompt(prompt: String): String {
        return prompt.trim().replace(Regex("\\s+"), " ").take(180)
    }

    private fun isCapabilityQuestion(normalized: String): Boolean {
        return listOf("capabilities", "what can you do", "what do you do", "help").any(normalized::contains)
    }

    private fun isPlanningQuestion(normalized: String): Boolean {
        return listOf("plan", "strategy", "organize", "steps", "roadmap").any(normalized::contains)
    }

    private fun isComparisonQuestion(normalized: String): Boolean {
        return listOf("compare", "difference", "better than", "versus", "vs ").any(normalized::contains)
    }

    private fun isReminderPrompt(normalized: String): Boolean {
        return listOf("remind", "task", "todo", "calendar", "alarm").any(normalized::contains)
    }

    private fun isCommunicationPrompt(normalized: String): Boolean {
        return listOf("call", "message", "sms", "text ", "email", "mail ").any(normalized::contains)
    }

    private fun isNavigationPrompt(normalized: String): Boolean {
        return listOf("open ", "launch ", "map", "navigate", "go to ", "settings").any(normalized::contains)
    }

    private fun isResearchPrompt(normalized: String): Boolean {
        return listOf("search", "look up", "browse", "analyze", "summarize", "research").any(normalized::contains)
    }
}
