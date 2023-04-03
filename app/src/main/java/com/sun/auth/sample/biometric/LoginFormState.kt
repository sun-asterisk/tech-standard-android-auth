package com.sun.auth.sample.biometric

/**
 * Data validation state of the signIn form.
 */
data class LoginFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
)
