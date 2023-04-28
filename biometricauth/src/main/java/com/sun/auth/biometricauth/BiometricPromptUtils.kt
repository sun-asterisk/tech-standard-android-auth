package com.sun.auth.biometricauth

import android.content.Context
import androidx.biometric.BiometricPrompt

object BiometricPromptUtils {

    /**
     * Create Biometric PromptInfo object with specified options.
     *
     * @param title required, title of prompt.
     * @param subtitle required, sub title of prompt.
     * @param description required, description of prompt.
     * @param confirmationRequired optional, Sets a system hint for whether to require explicit user
     *  confirmation after a passive biometric (e.g. iris or face) has been recognized,
     *  see [BiometricPrompt.PromptInfo.Builder.setConfirmationRequired].
     * @param negativeTextButton The label to be used for the negative button on the prompt.
     *  Note: only visible if not allow device credentials authentication (pin/password/pattern)
     * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
     * @return Biometric PromptInfo object with specified options.
     */
    @Suppress("LongParameterList")
    fun createPromptInfo(
        context: Context,
        title: String,
        subtitle: String,
        description: String,
        confirmationRequired: Boolean = false,
        negativeTextButton: String? = null,
        allowDeviceCredentials: Boolean = false,
    ): BiometricPrompt.PromptInfo {
        val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subtitle)
            setDescription(description)
            setConfirmationRequired(confirmationRequired)
        }

        val promptAuthenticators = context.getStrongestAuthenticators(allowDeviceCredentials)
        if (promptAuthenticators is StrongestAuthenticators.Available) {
            promptInfo.setAllowedAuthenticators((promptAuthenticators.authenticators))
            if (!promptAuthenticators.allowDeviceCredentials) {
                promptInfo.setNegativeButtonText(negativeTextButton ?: "Cancel")
            }
        }
        return promptInfo.build()
    }
}
