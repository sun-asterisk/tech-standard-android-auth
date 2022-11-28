package com.sun.sample.credentials

/**
 * Authentication result : success (user details) or error message.
 */
data class AuthenResult(
    val success: Token? = null,
    val error: Exception? = null
)