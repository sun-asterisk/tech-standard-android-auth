package com.sun.auth.credentials.results

import com.sun.auth.credentials.repositories.model.AuthToken
import okhttp3.Request

/**
 * Interface definition for a callback to be invoked when token is updated.
 */
interface AuthTokenChanged<T : AuthToken> {
    fun onTokenUpdate(token: T?): Request
}
