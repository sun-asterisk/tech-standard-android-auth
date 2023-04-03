package com.sun.auth.biometricauth

import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

object BiometricPromptUtils {
    private fun generateAuthenticationCallback(
        doOnError: (errCode: Int?, errString: CharSequence?) -> Unit,
        doOnSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    ): BiometricPrompt.AuthenticationCallback {
        return object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                doOnError.invoke(errCode, errString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                doOnError.invoke(null, null)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                doOnSuccess(result)
            }
        }
    }

    fun createBiometricPrompt(
        fragment: Fragment,
        doOnError: (errCode: Int?, errString: CharSequence?) -> Unit,
        doOnSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(fragment.requireActivity())
        return BiometricPrompt(
            fragment,
            executor,
            generateAuthenticationCallback(doOnError, doOnSuccess),
        )
    }

    fun createBiometricPrompt(
        activity: FragmentActivity,
        doOnError: (errCode: Int?, errString: CharSequence?) -> Unit,
        doOnSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)
        return BiometricPrompt(
            activity,
            executor,
            generateAuthenticationCallback(doOnError, doOnSuccess),
        )
    }

    fun createPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        confirmationRequired: Boolean = false,
        negativeTextButton: String,
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subtitle)
            setDescription(description)
            setConfirmationRequired(confirmationRequired)
            setNegativeButtonText(negativeTextButton)
        }.build()
    }

    fun createPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        confirmationRequired: Boolean = false,
        authenticators: Int,
    ): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subtitle)
            setDescription(description)
            setConfirmationRequired(confirmationRequired)
            setAllowedAuthenticators(authenticators)
        }.build()
    }
}
