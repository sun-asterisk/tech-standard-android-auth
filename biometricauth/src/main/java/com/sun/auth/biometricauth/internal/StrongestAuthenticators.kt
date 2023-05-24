package com.sun.auth.biometricauth.internal

import androidx.biometric.BiometricManager

internal sealed interface StrongestAuthenticators {
    /**
     * Biometric is supported and enrolled.
     */
    data class Available(val authenticators: Int) : StrongestAuthenticators {
        val allowDeviceCredentials: Boolean
            get() = authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL != 0
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
    object UnAvailable : StrongestAuthenticators

    /**
     * No biometric or device credential is enrolled in this devices
     */
    object NotEnrolled : StrongestAuthenticators
}
