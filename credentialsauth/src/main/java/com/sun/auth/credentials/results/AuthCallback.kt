package com.sun.auth.credentials.results

import com.sun.auth.credentials.repositories.model.AuthToken

/**
 * Interface definition for a callback to be invoked when do authentication via credentials.
 */
interface AuthCallback<T : AuthToken> {
    /**
     * Authentication result with response or error.
     * @param data [AuthToken] data or `null`.
     * @param error [Throwable] or `null`.
     */
    fun onResult(data: T? = null, error: Throwable? = null)
}
