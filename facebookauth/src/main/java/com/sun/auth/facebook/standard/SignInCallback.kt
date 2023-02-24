package com.sun.auth.facebook.standard

import com.facebook.AccessToken

/**
 * Interface definition for a callback to be invoked when do sign in via Facebook authentication.
 */
interface SignInCallback {
    /**
     * Sign in callback with data or error.
     * @param accessToken The [AccessToken] data or `null` if there is an error occurs.
     * @param error The [Throwable] when sign in error, it is `nullable`.
     */
    fun onResult(
        accessToken: AccessToken? = null,
        error: Throwable? = null,
    )
}
