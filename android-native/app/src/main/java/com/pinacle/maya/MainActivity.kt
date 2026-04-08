package com.pinacle.maya

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.pinacle.maya.assistant.MayaAssistantService
import com.pinacle.maya.capabilities.SystemUnlockManager
import com.pinacle.maya.capabilities.SystemCapabilityManager
import com.pinacle.maya.databinding.ActivityMainBinding
import com.pinacle.maya.memory.ConversationMemoryStore
import com.pinacle.maya.offline.DeviceActionRouter
import com.pinacle.maya.offline.OfflineInferenceCoordinator
import com.pinacle.maya.offline.VoiceSessionController
import com.pinacle.maya.runtime.ModelManifestStore
import com.pinacle.maya.runtime.RuntimeBundleStatusStore
import com.pinacle.maya.runtime.RuntimeProfileStore
import com.pinacle.maya.security.OwnerVoiceGate
import com.pinacle.maya.security.BiometricOwnerVerifier
import com.pinacle.maya.security.OwnerSessionStore
import com.pinacle.maya.security.PrivacyPolicyEngine
import com.pinacle.maya.security.PrivacyGuardian
import com.pinacle.maya.security.SecurityProfileStore

class MainActivity : AppCompatActivity() {
    private val runtimePermissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.POST_NOTIFICATIONS,
        android.Manifest.permission.READ_CONTACTS,
        android.Manifest.permission.READ_CALENDAR,
        android.Manifest.permission.WRITE_CALENDAR,
        android.Manifest.permission.CALL_PHONE,
        android.Manifest.permission.SEND_SMS,
        android.Manifest.permission.READ_PHONE_STATE,
        android.Manifest.permission.CAMERA
    )
    private lateinit var binding: ActivityMainBinding
    private lateinit var capabilityManager: SystemCapabilityManager
    private lateinit var unlockManager: SystemUnlockManager
    private lateinit var runtimeProfileStore: RuntimeProfileStore
    private lateinit var modelManifestStore: ModelManifestStore
    private lateinit var runtimeBundleStatusStore: RuntimeBundleStatusStore
    private lateinit var voiceSessionController: VoiceSessionController
    private lateinit var securityProfileStore: SecurityProfileStore
    private lateinit var ownerVoiceGate: OwnerVoiceGate
    private lateinit var privacyGuardian: PrivacyGuardian
    private lateinit var biometricOwnerVerifier: BiometricOwnerVerifier
    private lateinit var ownerSessionStore: OwnerSessionStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        capabilityManager = SystemCapabilityManager(this)
        unlockManager = SystemUnlockManager(this)
        runtimeProfileStore = RuntimeProfileStore(this)
        modelManifestStore = ModelManifestStore(this)
        runtimeBundleStatusStore = RuntimeBundleStatusStore(this)
        securityProfileStore = SecurityProfileStore(this)
        ownerVoiceGate = OwnerVoiceGate(securityProfileStore)
        privacyGuardian = PrivacyGuardian(this)
        biometricOwnerVerifier = BiometricOwnerVerifier(this)
        ownerSessionStore = OwnerSessionStore(this)
        voiceSessionController = VoiceSessionController(
            inferenceCoordinator = OfflineInferenceCoordinator(this),
            deviceActionRouter = DeviceActionRouter(this),
            memoryStore = ConversationMemoryStore(this),
            privacyPolicyEngine = PrivacyPolicyEngine(securityProfileStore, ownerSessionStore),
            privacyGuardian = privacyGuardian
        )
        privacyGuardian.applySecureWindow(this)
        binding.capabilitySummary.text = capabilityManager.describe()
        binding.hyperOsSummary.text = capabilityManager.hyperOsReadiness()
        binding.runtimeSummary.text = runtimeProfileStore.load().describe()
        binding.modelManifestSummary.text = modelManifestStore.loadRaw()
        binding.runtimeBundleSummary.text = runtimeBundleStatusStore.load().describe()
        binding.privilegeSummary.text = capabilityManager.privilegeSummary()
        binding.securitySummary.text = buildString {
            appendLine(biometricOwnerVerifier.availabilitySummary())
            appendLine(ownerVoiceGate.enrollmentStatus())
            appendLine(ownerSessionStore.summary())
            appendLine(privacyGuardian.statusSummary())
            append(privacyGuardian.cameraGuardianSummary())
        }
        binding.responsePreview.text = capabilityManager.setupChecklist()
        renderPrivacyShield()

        binding.startAssistantButton.setOnClickListener {
            val intent = Intent(this, MayaAssistantService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }

        binding.runPromptButton.setOnClickListener {
            val prompt = binding.promptInput.text?.toString()?.trim().orEmpty()
            if (prompt.isNotEmpty()) {
                binding.responsePreview.text = voiceSessionController.handlePrompt(prompt)
            }
        }

        binding.grantAccessButton.setOnClickListener {
            requestRuntimePermissions()
        }

        binding.notificationAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.accessibilityAccessButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.defaultAppsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS))
        }

        binding.unlockSystemsButton.setOnClickListener {
            val steps = mutableListOf<String>()
            if (unlockManager.requestAssistantRole(this)) {
                steps += "assistant role request opened"
            }
            if (unlockManager.requestDialerRole(this)) {
                steps += "dialer role request opened"
            }
            if (unlockManager.requestSmsRole(this)) {
                steps += "SMS role request opened"
            }
            if (unlockManager.requestBatteryOptimizationExemption(this)) {
                steps += "battery optimization exemption opened"
            }
            if (steps.isEmpty()) {
                unlockManager.openOverlaySettings(this)
                steps += "overlay settings opened"
            }
            binding.responsePreview.text = buildString {
                appendLine(unlockManager.unlockSummary())
                append("Triggered: ${steps.joinToString()}")
            }
            binding.privilegeSummary.text = capabilityManager.privilegeSummary()
        }

        binding.openUsageAccessButton.setOnClickListener {
            unlockManager.openUsageAccessSettings(this)
            binding.responsePreview.text = "Usage access settings opened for deeper activity awareness."
        }

        binding.openOverlayButton.setOnClickListener {
            unlockManager.openOverlaySettings(this)
            binding.responsePreview.text = "Overlay settings opened so Maya can request always-available assistant surfaces."
        }

        binding.enrollOwnerVoiceButton.setOnClickListener {
            val phrase = binding.ownerPhraseInput.text?.toString()?.trim().orEmpty()
            if (phrase.isBlank()) {
                binding.responsePreview.text = "Enter a private owner phrase first so Maya can protect owner-only voice actions."
            } else {
                securityProfileStore.saveOwnerPhrase(phrase)
                binding.securitySummary.text = buildString {
                    appendLine(biometricOwnerVerifier.availabilitySummary())
                    appendLine(ownerVoiceGate.enrollmentStatus())
                    appendLine(privacyGuardian.statusSummary())
                    append(privacyGuardian.cameraGuardianSummary())
                }
                binding.responsePreview.text = "Maya enrolled your fallback owner phrase. Real biometric owner verification can use the phone's enrolled face or device biometrics."
            }
        }

        binding.verifyOwnerBiometricButton.setOnClickListener {
            if (!biometricOwnerVerifier.canAuthenticate()) {
                binding.responsePreview.text = biometricOwnerVerifier.availabilitySummary()
            } else {
                biometricOwnerVerifier.prompt(
                    activity = this,
                    title = "Verify Maya owner",
                    subtitle = "Use your enrolled face, fingerprint, or device credential to unlock protected Maya actions.",
                    onAuthenticated = {
                        ownerSessionStore.markVerifiedNow()
                        renderPrivacyShield()
                        binding.securitySummary.text = buildString {
                            appendLine(biometricOwnerVerifier.availabilitySummary())
                            appendLine(ownerVoiceGate.enrollmentStatus())
                            appendLine(ownerSessionStore.summary())
                            appendLine(privacyGuardian.statusSummary())
                            append(privacyGuardian.cameraGuardianSummary())
                        }
                        binding.responsePreview.text = "Owner verified with real device biometrics."
                    },
                    onFailure = { message ->
                        binding.responsePreview.text = "Biometric verification did not succeed: $message"
                    }
                )
            }
        }

        binding.requestDeviceAdminButton.setOnClickListener {
            val opened = privacyGuardian.requestDeviceAdmin(this)
            binding.responsePreview.text = if (opened) {
                "Device admin request opened so Maya can lock the screen when privacy protection triggers."
            } else {
                "Maya already has device-admin control or the request could not be opened."
            }
            binding.securitySummary.text = buildString {
                appendLine(biometricOwnerVerifier.availabilitySummary())
                appendLine(ownerVoiceGate.enrollmentStatus())
                appendLine(ownerSessionStore.summary())
                appendLine(privacyGuardian.statusSummary())
                append(privacyGuardian.cameraGuardianSummary())
            }
        }

        binding.lockNowButton.setOnClickListener {
            val locked = privacyGuardian.lockScreenIfAllowed()
            binding.responsePreview.text = if (locked) {
                "Maya locked the screen."
            } else {
                "Maya still needs device-admin permission before it can lock the screen itself."
            }
        }

        binding.notificationPrivacyButton.setOnClickListener {
            privacyGuardian.openNotificationPrivacySettings(this)
            binding.responsePreview.text = "Notification privacy settings opened so you can hide sensitive previews from bystanders."
        }
    }

    private fun requestRuntimePermissions() {
        val missing = runtimePermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            binding.responsePreview.text = "Maya already has the main runtime permissions it can request directly. You can now enable notification access and accessibility for deeper control."
            return
        }
        ActivityCompat.requestPermissions(this, missing.toTypedArray(), 1001)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            val granted = permissions.zip(grantResults.toTypedArray())
                .count { it.second == PackageManager.PERMISSION_GRANTED }
            binding.capabilitySummary.text = capabilityManager.describe()
            binding.privilegeSummary.text = capabilityManager.privilegeSummary()
            binding.securitySummary.text = buildString {
                appendLine(biometricOwnerVerifier.availabilitySummary())
                appendLine(ownerVoiceGate.enrollmentStatus())
                appendLine(ownerSessionStore.summary())
                appendLine(privacyGuardian.statusSummary())
                append(privacyGuardian.cameraGuardianSummary())
            }
            renderPrivacyShield()
            binding.responsePreview.text = "Maya updated its permission state. Granted $granted of ${permissions.size} requested permissions."
        }
    }

    override fun onResume() {
        super.onResume()
        renderPrivacyShield()
    }

    private fun renderPrivacyShield() {
        val showShield = !ownerSessionStore.isRecentlyVerified()
        binding.privacyShieldOverlay.visibility = if (showShield) View.VISIBLE else View.GONE
        binding.privacyShieldOverlay.text = if (showShield) {
            "Maya privacy shield is active.\nSensitive details stay blurred until the owner verifies."
        } else {
            ""
        }
    }
}
