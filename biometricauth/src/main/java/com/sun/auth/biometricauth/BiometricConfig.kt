package com.sun.auth.biometricauth

import android.security.keystore.KeyProperties
import com.sun.auth.core.ConfigFunction

class BiometricConfig {
    /**
     * Allows the use of device credentials (PIN, Pattern, Password) as part of the authentication flow.
     */
    var allowDeviceCredentials: Boolean = false

    /**
     * The alias to use for the key in the Android KeyStore.
     */
    var keystoreAlias: String = "SUN_AUTH_BIOMETRIC_KEY_NAME"

    /**
     * The size of the key to generate (in bits). Default is 256.
     */
    var keySize: Int = 256

    /**
     * The algorithm to use for the key generation. Default is AES.
     * See [Cipher docs](https://developer.android.com/reference/javax/crypto/Cipher>) for more info.
     */
    var algorithm: String = KeyProperties.KEY_ALGORITHM_AES

    /**
     * The block mode to use for the key generation. Default is CBC.
     * See [Cipher docs](https://developer.android.com/reference/javax/crypto/Cipher>) for more info.
     */
    var blockMode: String = KeyProperties.BLOCK_MODE_CBC

    /**
     * The encryption padding to use for the key generation. Default is PKCS7.
     * See [Cipher docs](https://developer.android.com/reference/javax/crypto/Cipher>) for more info.
     */
    var padding: String = KeyProperties.ENCRYPTION_PADDING_PKCS7

    companion object {
        internal fun apply(
            allowDeviceCredentials: Boolean,
            setup: ConfigFunction<BiometricConfig>? = null,
        ): BiometricConfig {
            val config = BiometricConfig().apply {
                this.allowDeviceCredentials = allowDeviceCredentials
            }
            setup?.invoke(config)
            return config
        }
    }
}
