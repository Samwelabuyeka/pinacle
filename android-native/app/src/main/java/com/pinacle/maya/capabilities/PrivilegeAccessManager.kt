package com.pinacle.maya.capabilities

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.provider.Telephony
import android.provider.Settings
import android.telecom.TelecomManager
import android.app.role.RoleManager
import android.view.accessibility.AccessibilityManager
import com.pinacle.maya.assistant.MayaAccessibilityService
import com.pinacle.maya.assistant.MayaNotificationListenerService

class PrivilegeAccessManager(private val context: Context) {
    fun describe(): String {
        return buildString {
            appendLine("Deep access:")
            appendLine("Notification listener: ${status(notificationListenerEnabled())}")
            appendLine("Accessibility service: ${status(accessibilityEnabled())}")
            appendLine("Assistant role: ${status(assistantRoleHeld())}")
            appendLine("Default SMS app: ${status(defaultSmsApp())}")
            appendLine("Default dialer: ${status(defaultDialer())}")
            append("Battery optimization exemption: ${status(ignoringBatteryOptimizations())}")
        }
    }

    fun setupGuidance(): String {
        return buildString {
            appendLine("Deep control checklist:")
            appendLine("1. Turn on Maya notification access for live notification context.")
            appendLine("2. Turn on Maya accessibility service for deeper UI and system automation.")
            appendLine("3. Optionally set Maya as default SMS app if you want direct message handling.")
            append("4. Optionally set Maya as default dialer if you want faster call flows.")
        }
    }

    private fun notificationListenerEnabled(): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners").orEmpty()
        val component = ComponentName(context, MayaNotificationListenerService::class.java).flattenToString()
        return enabled.contains(component)
    }

    private fun accessibilityEnabled(): Boolean {
        val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager ?: return false
        val enabledServices = manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val myId = ComponentName(context, MayaAccessibilityService::class.java).flattenToShortString()
        return enabledServices.any { it.resolveInfo.serviceInfo?.let { info ->
            ComponentName(info.packageName, info.name).flattenToShortString() == myId
        } ?: false }
    }

    private fun defaultSmsApp(): Boolean {
        return Telephony.Sms.getDefaultSmsPackage(context) == context.packageName
    }

    private fun defaultDialer(): Boolean {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager ?: return false
        return telecomManager.defaultDialerPackage == context.packageName
    }

    private fun assistantRoleHeld(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        val roleManager = context.getSystemService(RoleManager::class.java) ?: return false
        return roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT) && roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)
    }

    private fun ignoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    private fun status(value: Boolean): String = if (value) "enabled" else "not enabled"
}
