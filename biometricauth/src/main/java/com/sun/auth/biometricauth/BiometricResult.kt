package com.sun.auth.biometricauth

import androidx.biometric.BiometricPrompt

sealed interface BiometricResult {
    data class Error(
        val errorCode: Int,
        val errorString: String,
    ) : BiometricResult, Throwable(errorString) {
        /**
         * Check that Biometric authentication is locked out.
         * @return true if temporary disabled short period or disabled until user unlocks via pass/pin/pattern
         */
        fun isBiometricLockout(): Boolean = errorCode == BiometricPrompt.ERROR_LOCKOUT ||
            errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
    }

    object Failed : BiometricResult
    data class BiometricRuntimeException(val exception: Throwable) : BiometricResult {
        fun isBiometricChangedError(): Boolean {
            return exception is UnableInitializeCipher || exception is UnableDecryptData
        }
    }
    data class Success(val result: BiometricPrompt.AuthenticationResult) : BiometricResult
}
