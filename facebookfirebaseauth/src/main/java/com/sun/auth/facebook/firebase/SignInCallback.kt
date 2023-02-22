package com.sun.auth.facebook.firebase

import com.google.firebase.auth.AuthResult

/**
 * Interface definition for a callback to be invoked when do sign in via Facebook authentication.
 */
interface SignInCallback {
    /**
     * Sign in callback with data or error.
     * @param authResult The [AuthResult] data or `null` if there is an error occurs.
     * @param error The [Throwable] when sign in error, it is `nullable`.
     */
    fun onResult(
        authResult: AuthResult? = null,
        error: Throwable? = null,
    )
}
