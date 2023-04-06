package com.sun.auth.biometricauth

import android.os.Build
import android.text.TextUtils
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

    internal fun createBiometricPrompt(
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

    internal fun createBiometricPrompt(
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
     * @param allowedAuthenticators optional, Specifies the type(s) of authenticators that may be
     *  invoked by BiometricPrompt to authenticate the user. See [BiometricPrompt.PromptInfo.Builder.setAllowedAuthenticators]
     * @return Biometric PromptInfo object with specified options.
     */
    @Suppress("LongParameterList")
    fun createPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        confirmationRequired: Boolean = false,
        negativeTextButton: String? = null,
        allowedAuthenticators: Int = 0,
    ): BiometricPrompt.PromptInfo {
        if (!AuthenticatorUtils.isSupportedCombination(allowedAuthenticators)) {
            throw IllegalArgumentException(
                "Authenticator combination is unsupported " +
                    "on API " + Build.VERSION.SDK_INT + ": " +
                    AuthenticatorUtils.convertToString(allowedAuthenticators),
            )
        }
        val isDeviceCredentialAllowed = if (allowedAuthenticators != 0) {
            AuthenticatorUtils.isDeviceCredentialAllowed(allowedAuthenticators)
        } else {
            false
        }

        require(!(TextUtils.isEmpty(negativeTextButton) && !isDeviceCredentialAllowed)) {
            "Negative text must be set and non-empty."
        }
        require(!(!TextUtils.isEmpty(negativeTextButton) && isDeviceCredentialAllowed)) {
            ("Negative text must not be set if device credential authentication is allowed.")
        }
        return BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(title)
            setSubtitle(subtitle)
            setDescription(description)
            setConfirmationRequired(confirmationRequired)
            negativeTextButton?.let(::setNegativeButtonText)
            allowedAuthenticators.let(::setAllowedAuthenticators)
        }.build()
    }
}
