package com.pinacle.maya.security

data class PrivacyDecision(
    val allowSensitiveReveal: Boolean,
    val blurResponse: Boolean,
    val requireOwnerVerification: Boolean,
    val shouldLockScreen: Boolean,
    val reason: String
)

class PrivacyPolicyEngine(
    private val securityProfileStore: SecurityProfileStore,
    private val ownerSessionStore: OwnerSessionStore
) {
    fun evaluate(prompt: String, requestedActions: List<String>): PrivacyDecision {
        val profile = securityProfileStore.load()
        val normalized = prompt.lowercase()
        val sensitivePrompt = listOf(
            "message", "sms", "email", "call", "contact", "calendar", "remind",
            "notification", "password", "code", "otp", "bank", "money", "location"
        ).any(normalized::contains)
        val sensitiveAction = requestedActions.any {
            it in setOf("send_sms", "call_contact", "create_reminder", "read_notifications", "send_email")
        }
        val sensitive = sensitivePrompt || sensitiveAction
        val ownerVerified = ownerSessionStore.isRecentlyVerified()
        val requireOwnerVerification = sensitive && !ownerVerified
        val blur = profile.privacyShieldEnabled && (requireOwnerVerification || profile.notificationBlurEnabled)
        val reason = when {
            requireOwnerVerification -> "Sensitive content is blocked until the owner verifies with biometrics."
            blur -> "Privacy shield is hiding sensitive details."
            else -> "No privacy restriction is active for this request."
        }
        return PrivacyDecision(
            allowSensitiveReveal = !requireOwnerVerification,
            blurResponse = blur,
            requireOwnerVerification = requireOwnerVerification,
            shouldLockScreen = false,
            reason = reason
        )
    }

    fun redact(text: String): String {
        if (text.isBlank()) return text
        return buildString {
            append("Protected by Maya privacy shield.\n")
            append("Verify owner identity to reveal details.\n")
            append("Preview length: ${text.length} characters")
        }
    }
}
