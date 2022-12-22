package com.sun.auth.social.callback

/**
 * Interface definition for a callback to be invoked when do sign out via Social authentication likes google, facebook, apple.. .
 */
interface SocialAuthSignOutCallback {
    /**
     * Sign out callback with success or error.
     * @param error The [Throwable] when sign out error, or `null` when sign out success.
     */
    fun onResult(error: Throwable? = null)
}
