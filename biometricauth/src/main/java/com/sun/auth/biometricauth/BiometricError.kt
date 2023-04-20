package com.sun.auth.biometricauth

sealed class BiometricException(
    val errorCode: Int?,
    message: String?,
    cause: Throwable? = null,
) : Throwable(message = message, cause = cause)

data class UnableEncryptData(private val originalCause: Throwable) : BiometricException(
    errorCode = 999,
    message = "The security on the device has changed and this key is no longer able to encrypt any data.",
    cause = originalCause,
)

data class UnableDecryptData(private val originalCause: Throwable) : BiometricException(
    errorCode = 998,
    message = "The security on the device has changed and this key is no longer able to to decrypt data",
    cause = originalCause,
)

data class UnableInitializeCipher(private val originalCause: Throwable?) : BiometricException(
    errorCode = 997,
    message = "The key has been rotated within the Android keystore and any ciphertext encrypted with the old key is no longer accessible.",
    cause = originalCause,
)
