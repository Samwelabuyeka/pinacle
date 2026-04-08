package com.pinacle.maya.security

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class BiometricOwnerVerifier(private val context: Context) {
    fun availabilitySummary(): String {
        return when (BiometricManager.from(context).canAuthenticate(authenticators())) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                "Real biometric owner verification is available. Maya can use enrolled face, fingerprint, or strong device biometrics."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "Biometric hardware exists, but no owner biometric is enrolled yet. Add face or fingerprint in system settings first."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "This phone does not report compatible biometric hardware for Maya."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Biometric hardware is temporarily unavailable right now."
            else ->
                "Biometric owner verification is not ready on this phone yet."
        }
    }

    fun canAuthenticate(): Boolean {
        return BiometricManager.from(context).canAuthenticate(authenticators()) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun prompt(
        activity: AppCompatActivity,
        title: String,
        subtitle: String,
        onAuthenticated: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onAuthenticated()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onFailure(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    onFailure("Biometric verification failed.")
                }
            }
        )
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators())
            .build()
        biometricPrompt.authenticate(promptInfo)
    }

    private fun authenticators(): Int {
        return BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}
