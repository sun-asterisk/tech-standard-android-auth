package com.sun.auth.biometricauth

import androidx.biometric.BiometricPrompt
import com.sun.auth.biometricauth.internal.UnableToDecryptData
import com.sun.auth.biometricauth.internal.UnableToInitializeCipher
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
        fun isBiometricLockout() = errorCode == BiometricPrompt.ERROR_LOCKOUT ||
            errorCode == BiometricPrompt.ERROR_LOCKOUT_PERMANENT
    }

    data class RuntimeException(val throwable: Throwable) : BiometricResult {
        /**
         * Check if the key is invalidated or not.
         * @return true if device's biometric settings was changed, then the key was invalidated.
         */
        fun isKeyInvalidatedError(): Boolean {
            return throwable is UnableToInitializeCipher || throwable is UnableToDecryptData
        }
    }

    object Failed : BiometricResult
}
