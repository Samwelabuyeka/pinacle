package com.pinacle.maya.assistant

import android.content.Context

class AssistantDeviceStateStore(context: Context) {
    private val preferences = context.getSharedPreferences("maya_device_state", Context.MODE_PRIVATE)

    fun saveLastNotification(summary: String) {
        preferences.edit().putString("last_notification", summary).apply()
    }

    fun saveLastAccessibilityEvent(summary: String) {
        preferences.edit().putString("last_accessibility_event", summary).apply()
    }

    fun savePrivacyEvent(summary: String) {
        preferences.edit().putString("last_privacy_event", summary).apply()
    }

    fun summarize(): String {
        val notification = preferences.getString("last_notification", "none") ?: "none"
        val accessibility = preferences.getString("last_accessibility_event", "none") ?: "none"
        val privacy = preferences.getString("last_privacy_event", "none") ?: "none"
        return "Last notification: $notification\nLast accessibility event: $accessibility\nLast privacy event: $privacy"
    }
}
