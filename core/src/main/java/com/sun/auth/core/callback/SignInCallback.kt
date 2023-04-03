package com.sun.auth.core.callback

/**
 * Interface definition for a callback to be invoked when do sign in via Google authentication
 */
interface SignInCallback<T> {
    /**
     * Sign in callback with data or error.
     * @param data The response data, `null` if there is an error occurs.
     * @param error The [Throwable] when sign in error, it is `nullable`.
     */
    fun onResult(
        data: T? = null,
        error: Throwable? = null,
    )
}
