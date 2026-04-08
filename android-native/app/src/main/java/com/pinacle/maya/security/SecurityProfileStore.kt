package com.pinacle.maya.security

import android.content.Context

data class SecurityProfile(
    val ownerPhrase: String,
    val privacyShieldEnabled: Boolean,
    val ownerLockEnabled: Boolean,
    val notificationBlurEnabled: Boolean,
    val frontCameraGuardianEnabled: Boolean
)

class SecurityProfileStore(context: Context) {
    private val preferences = context.getSharedPreferences("maya_security_profile", Context.MODE_PRIVATE)

    fun load(): SecurityProfile {
        return SecurityProfile(
            ownerPhrase = preferences.getString("owner_phrase", "") ?: "",
            privacyShieldEnabled = preferences.getBoolean("privacy_shield_enabled", true),
            ownerLockEnabled = preferences.getBoolean("owner_lock_enabled", true),
            notificationBlurEnabled = preferences.getBoolean("notification_blur_enabled", true),
            frontCameraGuardianEnabled = preferences.getBoolean("front_camera_guardian_enabled", true)
        )
    }

    fun saveOwnerPhrase(phrase: String) {
        preferences.edit().putString("owner_phrase", phrase.trim()).apply()
    }

    fun setPrivacyShieldEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("privacy_shield_enabled", enabled).apply()
    }

    fun setOwnerLockEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("owner_lock_enabled", enabled).apply()
    }

    fun setNotificationBlurEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("notification_blur_enabled", enabled).apply()
    }

    fun setFrontCameraGuardianEnabled(enabled: Boolean) {
        preferences.edit().putBoolean("front_camera_guardian_enabled", enabled).apply()
    }
}
