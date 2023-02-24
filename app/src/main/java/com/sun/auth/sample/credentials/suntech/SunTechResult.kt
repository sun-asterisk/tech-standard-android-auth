package com.sun.auth.sample.credentials.suntech

/**
 * Authentication result : success (user details) or error message.
 */
data class SunTechResult(
    val success: SunToken? = null,
    val error: Exception? = null,
)
