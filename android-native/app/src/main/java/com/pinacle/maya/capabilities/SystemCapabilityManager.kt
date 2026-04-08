package com.pinacle.maya.capabilities

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.pinacle.maya.bridges.HyperOsBridge

class SystemCapabilityManager(private val context: Context) {
    private val hyperOsBridge = HyperOsBridge()
    private val privilegeAccessManager = PrivilegeAccessManager(context)

    fun describe(): String {
        val lines = listOf(
            permissionLine("Microphone", Manifest.permission.RECORD_AUDIO),
            permissionLine("Notifications", Manifest.permission.POST_NOTIFICATIONS),
            permissionLine("Location", Manifest.permission.ACCESS_FINE_LOCATION),
            permissionLine("Calls", Manifest.permission.CALL_PHONE),
            permissionLine("SMS", Manifest.permission.SEND_SMS),
            permissionLine("Calendar", Manifest.permission.READ_CALENDAR),
            permissionLine("Contacts", Manifest.permission.READ_CONTACTS),
            permissionLine("Phone state", Manifest.permission.READ_PHONE_STATE),
            permissionLine("Audio settings", Manifest.permission.MODIFY_AUDIO_SETTINGS)
        )
        return lines.joinToString(separator = "\n")
    }

    fun hyperOsReadiness(): String {
        return buildString {
            appendLine(hyperOsBridge.describe())
            append("Deep Siri-like control still depends on public Android APIs, device-owner mode, OEM integrations, or privileged system-app deployment.")
        }
    }

    fun setupChecklist(): String {
        return buildString {
            appendLine("Recommended Maya setup:")
            appendLine("1. Grant microphone, notifications, contacts, calendar, and phone permissions.")
            appendLine("2. Enable Maya notification access in ${Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS}.")
            appendLine("3. Enable Maya accessibility service for deeper device control.")
            appendLine("4. Optionally set Maya as the preferred dialer or SMS assistant if you want faster communication flows.")
            appendLine()
            append(privilegeAccessManager.setupGuidance())
        }
    }

    fun privilegeSummary(): String = privilegeAccessManager.describe()

    fun siriParityMatrix(): String {
        val rows = listOf(
            capabilityLine("Wake word", granted = true, note = "Planned through openWakeWord."),
            capabilityLine("Voice replies", granted = true, note = "Planned through Piper or platform TTS."),
            capabilityLine("Reminders and tasks", granted = true, note = "Handled by Maya memory and task stores."),
            capabilityLine("Calls", granted = hasPermission(Manifest.permission.CALL_PHONE), note = "Requires dialer or call permissions."),
            capabilityLine("SMS drafting", granted = hasPermission(Manifest.permission.SEND_SMS), note = "Requires SMS permission or default SMS role."),
            capabilityLine("Notification reading", granted = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2, note = "Needs notification listener service."),
            capabilityLine("Calendar actions", granted = hasPermission(Manifest.permission.READ_CALENDAR), note = "Needs calendar permissions."),
            capabilityLine("App launch", granted = true, note = "Possible through explicit intents."),
            capabilityLine("Deep system toggles", granted = false, note = "Needs accessibility, device-owner mode, or OEM bridge."),
            capabilityLine("HyperOS extensions", granted = true, note = hyperOsBridge.describe())
        )
        return rows.joinToString(separator = "\n")
    }

    private fun permissionLine(label: String, permission: String): String {
        val granted = hasPermission(permission)
        return "$label: ${if (granted) "granted" else "not granted"}"
    }

    private fun capabilityLine(label: String, granted: Boolean, note: String): String {
        val status = if (granted) "available_or_unlockable" else "restricted"
        return "$label: $status ($note)"
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
