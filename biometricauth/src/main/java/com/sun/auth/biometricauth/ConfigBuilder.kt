package com.sun.auth.biometricauth

import android.app.Application
import com.sun.auth.core.invoke

/**
 * Setup Biometric Authentication config.
 * @param allowDeviceCredentials Allows the use of device credentials (PIN, Pattern, Password) as part of the authentication flow.
 * @param setup Other Biometric optional configurations for key generation, cryptography, etc.
 */
fun Application.initBiometricAuth(
    allowDeviceCredentials: Boolean,
    setup: BiometricConfig.() -> Unit = {},
) {
    val config = BiometricConfig.apply(allowDeviceCredentials, invoke(setup))
    BiometricAuth.initialize(this, config)
}
