package com.sun.auth.sample.credentials

/**
 * Authentication result : success or error.
 */
data class AuthResult(
    val success: Token? = null,
    val error: Throwable? = null,
)
