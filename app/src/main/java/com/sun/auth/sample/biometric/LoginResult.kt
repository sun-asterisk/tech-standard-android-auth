package com.sun.auth.sample.biometric

import com.sun.auth.sample.model.Token

/**
 * Authentication result : token (success) or error.
 */
data class LoginResult(
    val token: Token? = null,
    val error: Throwable? = null,
)
