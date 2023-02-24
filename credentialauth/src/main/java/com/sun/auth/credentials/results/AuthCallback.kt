package com.sun.auth.credentials.results

import com.sun.auth.credentials.repositories.model.AuthToken

/**
 * Interface definition for a callback to be invoked when do authentication via credentials.
 */
interface AuthCallback<T : AuthToken> {
    /**
     * Authentication success callback with response.
     * @param data [AuthToken] data or `null`.
     */
    fun success(data: T?)

    /**
     * Authentication failure callback with exception.
     * @param exception [AuthException] data or `null`
     */
    fun failure(exception: AuthException?)
}
