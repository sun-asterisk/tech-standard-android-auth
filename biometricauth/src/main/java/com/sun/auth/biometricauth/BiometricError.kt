package com.sun.auth.biometricauth

import androidx.biometric.BiometricManager

/**
 * Extras errors code and message when process Biometric. For other default errors, see [BiometricManager]
 */
object BiometricError {
    /**
     * Occur when initialize cipher process is fail
     */
    const val ERROR_NO_CIPHER_CREATED = 100

    /**
     * Occur when you try to use biometric authentication (DECRYPT) when this biometric is [BiometricMode.OFF].
     */
    const val ERROR_BIOMETRIC_MODE_IS_OFF = 101

    const val ERROR_BIOMETRIC_NOT_SET = 102

    const val MESSAGE_NO_CIPHER_CREATED = "No cipher is created!"
    const val MESSAGE_BIOMETRIC_MODE_IS_OFF = "Biometric mode is off!"

    /**
     * Common error message when authenticate is not [BiometricManager.BIOMETRIC_SUCCESS] status.
     *
     * See all definitions error code in [BiometricManager] class.
     */
    const val MESSAGE_BIOMETRIC_PROCESS_FAIL = "Biometric authenticate process failed!"
    const val MESSAGE_NO_BIOMETRIC_SETTINGS = "Seems biometric is not set!"
}
