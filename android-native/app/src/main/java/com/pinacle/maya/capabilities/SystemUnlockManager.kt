package com.pinacle.maya.capabilities

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

class SystemUnlockManager(private val context: Context) {
    fun requestAssistantRole(activity: Activity): Boolean {
        val roleManager = roleManagerOrNull() ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager.isRoleAvailable(RoleManager.ROLE_ASSISTANT) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_ASSISTANT)
        ) {
            activity.startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_ASSISTANT))
            return true
        }
        return false
    }

    fun requestDialerRole(activity: Activity): Boolean {
        val roleManager = roleManagerOrNull() ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        ) {
            activity.startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER))
            return true
        }
        return false
    }

    fun requestSmsRole(activity: Activity): Boolean {
        val roleManager = roleManagerOrNull() ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            roleManager.isRoleAvailable(RoleManager.ROLE_SMS) &&
            !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
        ) {
            activity.startActivity(roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS))
            return true
        }
        return false
    }

    fun requestBatteryOptimizationExemption(activity: Activity): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager ?: return false
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            activity.startActivity(intent)
            return true
        }
        return false
    }

    fun openOverlaySettings(activity: Activity) {
        activity.startActivity(
            Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${context.packageName}")
            )
        )
    }

    fun openUsageAccessSettings(activity: Activity) {
        activity.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }

    fun unlockSummary(): String {
        return buildString {
            appendLine("Unlockable deep access:")
            appendLine("Assistant role: requestable on supported Android builds.")
            appendLine("Dialer role: requestable on supported Android builds.")
            appendLine("SMS role: requestable on supported Android builds.")
            appendLine("Battery optimization exemption: requestable.")
            appendLine("Overlay permission: requestable through system settings.")
            append("Usage access: requestable through system settings.")
        }
    }

    private fun roleManagerOrNull(): RoleManager? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
        } else {
            null
        }
    }
}
