package com.sun.auth.sample.credentials.other

/**
 * Authentication result : success (user details) or error message.
 */
data class CredentialAuthResult(
    val success: Token? = null,
    val error: Exception? = null,
)
