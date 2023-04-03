package com.sun.auth.biometricauth

enum class BiometricMode {
    /**
     * This mode is used when you try to verify biometric before enable biometric and encrypt something.
     *
     * Ex: Verify and encrypt the response Token to enable Setting biometric authentication.
     */
    ON,

    /**
     * This mode is used when you try to verify biometric before turn off the biometric
     *
     * Ex: Verify and turn of biometric authentication settings.
     */
    OFF,

    /**
     * This mode is used when you try to verify biometric before decrypt something.
     *
     * Ex: Verify and decrypt the encrypted Token and authenticate the app.
     */
    DECRYPT,
}
