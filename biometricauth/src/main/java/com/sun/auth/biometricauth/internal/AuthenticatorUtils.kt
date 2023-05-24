package com.sun.auth.biometricauth.internal

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

internal fun BiometricManager.mapToStrongestAuthenticators(authenticators: Int): StrongestAuthenticators {
    return when (canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> StrongestAuthenticators.Available(authenticators)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> StrongestAuthenticators.InsecureHardWare
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> StrongestAuthenticators.NotEnrolled
        else -> StrongestAuthenticators.UnAvailable
        // else cases
        // BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE,
        // BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
        // BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED,
        // BiometricManager.BIOMETRIC_STATUS_UNKNOWN
    }
}

internal fun Context.getStrongestAuthenticators(allowDeviceCredentials: Boolean): StrongestAuthenticators {
    val biometricManager = BiometricManager.from(this)
    val authenticatorsToTry =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && allowDeviceCredentials) {
            arrayListOf(
                BIOMETRIC_STRONG or DEVICE_CREDENTIAL,
                BIOMETRIC_STRONG,
                BIOMETRIC_WEAK or DEVICE_CREDENTIAL,
                BIOMETRIC_WEAK,
                DEVICE_CREDENTIAL,
            )
        } else {
            arrayListOf(BIOMETRIC_STRONG, BIOMETRIC_WEAK)
        }
    authenticatorsToTry.forEach { authenticator ->
        val outcome = biometricManager.mapToStrongestAuthenticators(authenticator)
        if (outcome !is StrongestAuthenticators.UnAvailable) {
            return outcome
        }
    }
    return StrongestAuthenticators.UnAvailable
}
