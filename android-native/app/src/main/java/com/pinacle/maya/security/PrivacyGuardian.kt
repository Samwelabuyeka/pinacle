package com.pinacle.maya.security

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.view.WindowManager
import com.pinacle.maya.assistant.AssistantDeviceStateStore

class PrivacyGuardian(private val context: Context) {
    private val securityProfileStore = SecurityProfileStore(context)
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
    private val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)
    private val stateStore = AssistantDeviceStateStore(context)

    fun applySecureWindow(activity: Activity) {
        if (securityProfileStore.load().privacyShieldEnabled) {
            activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun requestDeviceAdmin(activity: Activity): Boolean {
        if (devicePolicyManager?.isAdminActive(adminComponent) == true) {
            return false
        }
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enable Maya to lock the screen when privacy protection is triggered."
            )
        }
        activity.startActivity(intent)
        return true
    }

    fun lockScreenIfAllowed(): Boolean {
        if (devicePolicyManager?.isAdminActive(adminComponent) == true) {
            devicePolicyManager.lockNow()
            return true
        }
        return false
    }

    fun openNotificationPrivacySettings(activity: Activity) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_SETTINGS)
        }
        activity.startActivity(intent)
    }

    fun markBystanderDetected(reason: String) {
        stateStore.savePrivacyEvent("bystander_view | $reason")
    }

    fun markHolderMismatch(reason: String) {
        stateStore.savePrivacyEvent("holder_mismatch | $reason")
    }

    fun handleHolderMismatch(reason: String): Boolean {
        markHolderMismatch(reason)
        val profile = securityProfileStore.load()
        return if (profile.ownerLockEnabled) {
            lockScreenIfAllowed()
        } else {
            false
        }
    }

    fun statusSummary(): String {
        val profile = securityProfileStore.load()
        val adminActive = devicePolicyManager?.isAdminActive(adminComponent) == true
        return buildString {
            appendLine("Privacy shield: ${if (profile.privacyShieldEnabled) "enabled" else "disabled"}")
            appendLine("Owner lock: ${if (profile.ownerLockEnabled) "enabled" else "disabled"}")
            appendLine("Notification blur: ${if (profile.notificationBlurEnabled) "enabled" else "disabled"}")
            appendLine("Front camera guardian: ${if (profile.frontCameraGuardianEnabled) "enabled" else "disabled"}")
            append("Device admin lock: ${if (adminActive) "enabled" else "not enabled"}")
        }
    }

    fun cameraGuardianSummary(): String {
        return "Bystander viewing should stay blur-only. A real holder-mismatch event can lock the phone once a local owner-vs-non-owner vision model is bundled."
    }
}
