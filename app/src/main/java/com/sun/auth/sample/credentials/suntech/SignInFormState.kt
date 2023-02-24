package com.sun.auth.sample.credentials.suntech

/**
 * Data validation state of the signIn form.
 */
data class SignInFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
)
