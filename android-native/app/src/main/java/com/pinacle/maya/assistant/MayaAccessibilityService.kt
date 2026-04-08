package com.pinacle.maya.assistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

class MayaAccessibilityService : AccessibilityService() {
    private val stateStore by lazy { AssistantDeviceStateStore(this) }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val summary = buildString {
            append(event?.packageName ?: "unknown_package")
            append(" | ")
            append(event?.className ?: "unknown_class")
            append(" | ")
            append(event?.text?.joinToString(" ") ?: "no_text")
        }
        stateStore.saveLastAccessibilityEvent(summary)
    }

    override fun onInterrupt() = Unit
}
