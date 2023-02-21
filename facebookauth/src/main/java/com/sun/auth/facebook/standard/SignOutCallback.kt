package com.sun.auth.facebook.standard

/**
 * Interface definition for a callback to be invoked when do sign out via Facebook authentication.
 */
interface SignOutCallback {
    /**
     * Sign out callback with success or error.
     * @param error The [Throwable] when sign out error, or `null` when sign out success.
     */
    fun onResult(error: Throwable? = null)
}
