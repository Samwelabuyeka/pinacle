package com.pinacle.maya.offline

data class AssistantPlan(
    val requestedActions: List<String>,
    val actionArguments: Map<String, String>,
    val shouldEscalateToHeavyModel: Boolean,
    val responseMode: String
)

class AssistantActionPlanner {
    fun plan(prompt: String): AssistantPlan {
        val normalized = prompt.lowercase()
        val actionArguments = mutableMapOf<String, String>()
        val actions = buildList {
            if ("remind" in normalized) add("create_reminder")
            if ("alarm" in normalized) add("create_alarm")
            if ("call" in normalized) add("call_contact")
            if ("message" in normalized || "sms" in normalized || "text " in normalized) add("send_sms")
            if ("notification" in normalized) add("read_notifications")
            if ("open " in normalized || "launch " in normalized) add("open_app")
            if ("browse " in normalized || "go to " in normalized || "open website" in normalized) add("browse_web")
            if ("search " in normalized || "look up " in normalized || "google " in normalized) add("search_web")
            if ("map " in normalized || "directions" in normalized || "navigate" in normalized) add("open_maps")
            if ("email " in normalized || "mail " in normalized) add("send_email")
            if ("settings" in normalized || "permissions" in normalized) add("open_settings")
            if ("default app" in normalized || "default apps" in normalized || "dialer role" in normalized || "sms role" in normalized) add("open_default_apps")
            if ("capabilities" in normalized || "what can you do" in normalized) add("summarize_capabilities")
            if ("privilege" in normalized || "access status" in normalized || "deep access" in normalized) add("check_privileges")
        }
        extractPhoneNumber(prompt)?.let { actionArguments["phoneNumber"] = it }
        extractMessageBody(prompt)?.let { actionArguments["message"] = it }
        extractAppName(prompt)?.let { actionArguments["appName"] = it }
        extractReminderTitle(prompt)?.let { actionArguments["title"] = it }
        extractWebTarget(prompt)?.let { actionArguments["webTarget"] = it }
        extractSearchQuery(prompt)?.let { actionArguments["query"] = it }
        extractEmailBody(prompt)?.let { actionArguments["emailBody"] = it }
        val shouldEscalate = listOf(
            "plan",
            "strategy",
            "analyze",
            "compare",
            "summarize",
            "deep",
            "complex"
        ).any { it in normalized }
        val responseMode = if (actions.isNotEmpty()) "action_first" else "conversation_first"
        return AssistantPlan(
            requestedActions = actions,
            actionArguments = actionArguments,
            shouldEscalateToHeavyModel = shouldEscalate,
            responseMode = responseMode
        )
    }

    private fun extractPhoneNumber(prompt: String): String? {
        val digits = prompt.filter { it.isDigit() || it == '+' }
        return digits.takeIf { it.count(Char::isDigit) >= 7 }
    }

    private fun extractMessageBody(prompt: String): String? {
        val normalized = prompt.lowercase()
        val marker = listOf("message ", "text ", "sms ").firstOrNull { normalized.contains(it) } ?: return null
        return prompt.substringAfter(marker, "").trim().takeIf { it.isNotEmpty() }
    }

    private fun extractAppName(prompt: String): String? {
        val normalized = prompt.lowercase()
        val marker = listOf("open ", "launch ").firstOrNull { normalized.contains(it) } ?: return null
        return prompt.substringAfter(marker, "").trim().takeIf { it.isNotEmpty() }
    }

    private fun extractReminderTitle(prompt: String): String? {
        val normalized = prompt.lowercase()
        if (!normalized.contains("remind")) return null
        return prompt.substringAfter("to", "").trim().takeIf { it.isNotEmpty() }
    }

    private fun extractWebTarget(prompt: String): String? {
        val normalized = prompt.lowercase()
        val marker = listOf("browse ", "go to ", "open website ").firstOrNull { normalized.contains(it) } ?: return null
        return prompt.substringAfter(marker, "").trim().takeIf { it.isNotEmpty() }
    }

    private fun extractSearchQuery(prompt: String): String? {
        val normalized = prompt.lowercase()
        val marker = listOf("search ", "look up ", "google ").firstOrNull { normalized.contains(it) } ?: return null
        return prompt.substringAfter(marker, "").trim().takeIf { it.isNotEmpty() }
    }

    private fun extractEmailBody(prompt: String): String? {
        val normalized = prompt.lowercase()
        if (!normalized.contains("email") && !normalized.contains("mail")) return null
        return prompt.substringAfter("email", prompt.substringAfter("mail", "")).trim().takeIf { it.isNotEmpty() }
    }
}
