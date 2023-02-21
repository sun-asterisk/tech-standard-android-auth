package com.sun.auth.google.standard

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

/**
 * Interface definition for a callback to be invoked when do sign in via Google authentication
 */
interface SignInCallback {
    /**
     * Sign in callback with data or error.
     * @param account The [GoogleSignInAccount] data, `null` if there is an error occurs.
     * @param error The [Throwable] when sign in error, it is `nullable`.
     */
    fun onResult(
        account: GoogleSignInAccount? = null,
        error: Throwable? = null,
    )
}
