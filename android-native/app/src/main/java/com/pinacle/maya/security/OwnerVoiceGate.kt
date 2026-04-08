package com.pinacle.maya.security

class OwnerVoiceGate(
    private val securityProfileStore: SecurityProfileStore
) {
    fun verifyTranscribedOwnerPhrase(transcribedText: String): Boolean {
        val enrolledPhrase = securityProfileStore.load().ownerPhrase.trim().lowercase()
        if (enrolledPhrase.isBlank()) {
            return false
        }
        return transcribedText.trim().lowercase().contains(enrolledPhrase)
    }

    fun enrollmentStatus(): String {
        val enrolled = securityProfileStore.load().ownerPhrase.isNotBlank()
        return if (enrolled) {
            "Owner voice gate is enrolled with a local unlock phrase. A real speaker-verification model is still needed for true voice biometrics."
        } else {
            "Owner voice gate is not enrolled yet. Save a private unlock phrase for fallback owner checks until a speaker-verification model is bundled."
        }
    }
}
