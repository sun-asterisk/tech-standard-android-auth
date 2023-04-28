package com.sun.auth.biometricauth

const val ERROR_UNABLE_TO_ENCRYPT = 999
const val ERROR_UNABLE_TO_DECRYPT = 998
const val ERROR_UNABLE_TO_INIT_CIPHER = 997

internal sealed class BiometricError(
    val errorCode: Int?,
    message: String?,
    cause: Throwable? = null,
) : Throwable(message = message, cause = cause)

internal data class UnableToEncryptData(private val originalCause: Throwable) :
    BiometricError(
        errorCode = ERROR_UNABLE_TO_ENCRYPT,
        message = "The security on the device has changed and this key is no longer " +
            "able to encrypt any data.",
        cause = originalCause,
    )

internal data class UnableToDecryptData(private val originalCause: Throwable) :
    BiometricError(
        errorCode = ERROR_UNABLE_TO_DECRYPT,
        message = "The security on the device has changed and this key is no longer " +
            "able to decrypt any data",
        cause = originalCause,
    )

internal data class UnableToInitializeCipher(private val originalCause: Throwable?) :
    BiometricError(
        errorCode = ERROR_UNABLE_TO_INIT_CIPHER,
        message = "The key has been rotated within the Android keystore and any encrypted data " +
            "with the old key is no longer accessible.",
        cause = originalCause,
    )
