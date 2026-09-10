package com.example.util

import android.content.Context
import android.content.ContextWrapper
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricAuthManager {

    sealed class BiometricAvailability {
        object Available : BiometricAvailability()
        data class Unavailable(val reason: String, val canPromptDeviceCredential: Boolean) : BiometricAvailability()
    }

    /**
     * Check if hardware biometric or device lock credential is ready and supported.
     */
    fun checkBiometricAvailability(context: Context): BiometricAvailability {
        val biometricManager = BiometricManager.from(context)
        val canAuthBiometric = biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK)
        val canAuthCredential = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

        return when {
            canAuthBiometric == BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricAvailability.Available
            }
            canAuthCredential == BiometricManager.BIOMETRIC_SUCCESS -> {
                BiometricAvailability.Available
            }
            canAuthBiometric == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                BiometricAvailability.Unavailable(
                    reason = "No fingerprint or screen lock enrolled on device.",
                    canPromptDeviceCredential = false
                )
            }
            canAuthBiometric == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                BiometricAvailability.Unavailable(
                    reason = "No biometric scanner hardware present.",
                    canPromptDeviceCredential = canAuthCredential == BiometricManager.BIOMETRIC_SUCCESS
                )
            }
            canAuthBiometric == BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                BiometricAvailability.Unavailable(
                    reason = "Biometric sensor currently unavailable.",
                    canPromptDeviceCredential = false
                )
            }
            else -> {
                BiometricAvailability.Unavailable(
                    reason = "Biometric authentication not supported.",
                    canPromptDeviceCredential = false
                )
            }
        }
    }

    /**
     * Finds the parent FragmentActivity from any Context wrapper chain.
     */
    fun findFragmentActivity(context: Context): FragmentActivity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is FragmentActivity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Launches the official Android BiometricPrompt prompt.
     */
    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "Biometric Verification",
        subtitle: String = "Verify identity to record attendance",
        negativeButtonText: String = "Cancel",
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onError(errorCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailed()
            }
        }

        val prompt = BiometricPrompt(activity, executor, callback)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription("Touch fingerprint sensor or scan face to confirm Presence attendance")
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            .build()

        prompt.authenticate(promptInfo)
    }
}
