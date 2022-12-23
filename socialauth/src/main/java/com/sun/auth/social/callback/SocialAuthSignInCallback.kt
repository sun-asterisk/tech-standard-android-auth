package com.sun.auth.social.callback

import com.sun.auth.social.model.SocialUser

/**
 * Interface definition for a callback to be invoked when do sign in via Social authentication likes google, facebook, apple.. .
 */
interface SocialAuthSignInCallback {
    /**
     * Sign in callback with data or error.
     * @param user The [SocialUser] data, `null` if there is an error occurs.
     * @param error The [Throwable] when sign in error, it is `nullable`.
     */
    fun onResult(user: SocialUser?, error: Throwable?)
}
