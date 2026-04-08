package com.pinacle.maya.security

import android.content.Context

class OwnerSessionStore(context: Context) {
    private val preferences = context.getSharedPreferences("maya_owner_session", Context.MODE_PRIVATE)

    fun markVerifiedNow() {
        preferences.edit().putLong("verified_at_ms", System.currentTimeMillis()).apply()
    }

    fun clear() {
        preferences.edit().remove("verified_at_ms").apply()
    }

    fun isRecentlyVerified(windowMs: Long = 3 * 60 * 1000L): Boolean {
        val verifiedAt = preferences.getLong("verified_at_ms", 0L)
        return verifiedAt > 0L && (System.currentTimeMillis() - verifiedAt) <= windowMs
    }

    fun summary(): String {
        return if (isRecentlyVerified()) {
            "Owner session is verified for privacy-sensitive actions."
        } else {
            "Owner session is not verified right now. Sensitive Maya actions should stay hidden or require biometric confirmation."
        }
    }
}
