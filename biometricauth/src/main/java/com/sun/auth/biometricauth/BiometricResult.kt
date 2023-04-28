package com.sun.auth.biometricauth

import androidx.biometric.BiometricPrompt
import javax.crypto.Cipher

sealed interface BiometricResult {
    data class Success(val result: BiometricPrompt.AuthenticationResult) : BiometricResult {
        fun getCipher(): Cipher? {
            return result.cryptoObject?.cipher
        }
    }

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

    data class BiometricRuntimeException(val exception: Throwable) : BiometricResult {
        /**
         * Check device's biometric settings was changed or not.
         * @return true if device's biometric settings was changed.
         */
        fun isBiometricChangedError(): Boolean {
            return exception is UnableToInitializeCipher || exception is UnableToDecryptData
        }
    }

    object Failed : BiometricResult
}
