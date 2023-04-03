package com.sun.auth.sample.credentials

import com.sun.auth.sample.model.Token

/**
 * Authentication result : success or error.
 */
data class AuthResult(
    val success: Token? = null,
    val error: Throwable? = null,
)
