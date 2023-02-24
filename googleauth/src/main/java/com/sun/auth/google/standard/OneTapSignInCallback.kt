package com.sun.auth.google.standard

import com.google.android.gms.auth.api.identity.SignInCredential

/**
 * Interface definition for a callback to be invoked when do sign in via Google authentication using OneTapSignIn api.
 */
interface OneTapSignInCallback {
    /**
     * Result callback with success or error.
     * @param credential The [SignInCredential] response from OneTap SignIn process.
     * @param error The [Throwable] when sign out error, or `null` when sign out success.
     */
    fun onResult(
        credential: SignInCredential? = null,
        error: Throwable? = null,
    )
}
