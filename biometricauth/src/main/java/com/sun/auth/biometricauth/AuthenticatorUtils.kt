package com.sun.auth.biometricauth

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL

sealed interface StrongestAuthenticators {
    /**
     * Biometric is supported and enrolled.
     */
    data class Available(val authenticators: Int) : StrongestAuthenticators {
        val allowDeviceCredentials: Boolean
            get() = authenticators and DEVICE_CREDENTIAL != 0
    }

    /**
     * The user can't authenticate because a security vulnerability has been discovered with one
     * or more hardware sensors. The affected sensor(s) are unavailable until a security update
     * has addressed the issue.
     */
    object InsecureHardWare : StrongestAuthenticators

    /**
     * No hardware biometric is supported in this devices.
     */
    object NotAvailable : StrongestAuthenticators

    /**
     * No biometric or device credential is enrolled in this devices
     */
    object NotEnrolled : StrongestAuthenticators
}

internal fun BiometricManager.mapToStrongestAuthenticators(authenticators: Int): StrongestAuthenticators {
    return when (canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> StrongestAuthenticators.Available(authenticators)
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> StrongestAuthenticators.InsecureHardWare
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> StrongestAuthenticators.NotEnrolled
        else -> StrongestAuthenticators.NotAvailable
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
        if (outcome !is StrongestAuthenticators.NotAvailable) {
            return outcome
        }
    }
    return StrongestAuthenticators.NotAvailable
}

/**
 * Check whether the biometric is available and enrolled
 * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
 * @return true if available and enrolled.
 */
fun Context?.isBiometricAvailable(allowDeviceCredentials: Boolean = false): Boolean {
    return this?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.Available
}

/**
 * Check whether the biometric is unavailable or unsupported on this device
 * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
 * @return true if unavailable or unsupported
 */
fun Context?.isBiometricUnAvailable(allowDeviceCredentials: Boolean = false): Boolean {
    return this?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.NotAvailable
}

/**
 * Check whether the biometric is insecure or not
 * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
 * @return true if biometric is insecure
 */
fun Context?.isBiometricInsecure(allowDeviceCredentials: Boolean = false): Boolean {
    return this?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.InsecureHardWare
}

/**
 * Check whether the biometric is enrolled or not
 * @param allowDeviceCredentials true if allow PIN/Pattern/Password to login
 * @return true if biometric is enrolled.
 */
fun Context?.isBiometricNotEnrolled(allowDeviceCredentials: Boolean = false): Boolean {
    return this?.getStrongestAuthenticators(allowDeviceCredentials) is StrongestAuthenticators.NotEnrolled
}
