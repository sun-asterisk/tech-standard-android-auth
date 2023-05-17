package com.sun.auth.sample.biometrics.home

/**
 * Data validation state of the login form.
 */
data class BiometricState(
    val isBiometricAvailable: Boolean = true,
    val isBiometricChecked: Boolean = false,
)
