package com.sun.auth.biometricauth.internal

internal sealed class BiometricError(
    message: String?,
    cause: Throwable? = null,
) : Throwable(message = message, cause = cause)

internal data class UnableToEncryptData(private val originalCause: Throwable?) :
    BiometricError(
        message = "The security on the device has changed and this key is no longer " +
            "able to encrypt any data.",
        cause = originalCause,
    )

internal data class UnableToDecryptData(private val originalCause: Throwable?) :
    BiometricError(
        message = "The security on the device has changed and this key is no longer " +
            "able to decrypt any data",
        cause = originalCause,
    )

internal data class UnableToInitializeCipher(private val originalCause: Throwable?) :
    BiometricError(
        message = "The key has been rotated within the Android keystore and any encrypted data " +
            "with the old key is no longer accessible.",
        cause = originalCause,
    )
