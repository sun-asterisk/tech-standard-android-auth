package com.sun.auth.credentials.interceptors

import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.utils.BEARER
import com.sun.auth.credentials.utils.TOKEN_AUTHORIZATION
import kotlinx.coroutines.runBlocking
import okhttp3.* // ktlint-disable no-wildcard-imports
import retrofit2.HttpException

/**
 * The automatically refresh token authenticator when added this to your [OkHttpClient]. Ex:
 * ```kt
 * client = OkHttpClient().newBuilder()
 *          .authenticator(TokenAuthenticator<AuthToken>())
 *          ...
 *          .build()
 * ```
 *
 * @param refreshTokenExpiredErrorCode The defined server error code about RefreshToken is expired.
 */
class TokenAuthenticator<T : AuthToken>(
    private val refreshTokenExpiredErrorCode: Int = Int.MIN_VALUE,
) : Authenticator {
    private val auth: CredentialsAuth by lazy { CredentialsAuth.getInstance() }

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= RETRY_COUNT_LIMIT) {
            doOnRefreshTokenFailed()
            return null
        }
        val oldToken = auth.getToken<T>()

        synchronized(this) {
            // Prevent multiple unnecessary refresh token calls
            val syncedToken = auth.getToken<T>()

            if (oldToken != null && oldToken.crAccessToken != syncedToken?.crAccessToken) {
                // if current refresh token is different from saved, that means another refresh has done.
                // we just use the new access token
                return if (syncedToken != null) {
                    response.request.newBuilder()
                        .header(TOKEN_AUTHORIZATION, "$BEARER ${syncedToken.crAccessToken}")
                        .build()
                } else {
                    null
                }
            }

            return runBlocking {
                try {
                    val newToken = auth.refreshToken<T>()?.apply {
                        // We still keep old refresh token if new version is null
                        if (crRefreshToken.isNullOrBlank()) {
                            crRefreshToken = oldToken?.crRefreshToken
                        }
                    }
                    auth.saveToken(newToken)

                    response.request.newBuilder()
                        .header(TOKEN_AUTHORIZATION, "$BEARER ${newToken?.crAccessToken}")
                        .build()
                } catch (exception: Exception) {
                    if (isRefreshTokenExpired(exception)) {
                        // Skip retry
                        doOnRefreshTokenFailed()
                    }
                    null
                }
            }
        }
    }

    private fun isRefreshTokenExpired(exception: Exception): Boolean {
        return exception is HttpException &&
            exception.code() != Int.MIN_VALUE &&
            exception.code() == refreshTokenExpiredErrorCode
    }

    private fun responseCount(response: Response): Int {
        var responseMutable = response.priorResponse
        var count = 1
        while (responseMutable != null) {
            count++
            responseMutable = response.priorResponse
        }
        return count
    }

    private fun doOnRefreshTokenFailed() {
        auth.run {
            removeToken()
            notifyTokenExpired()
        }
    }

    companion object {
        private const val RETRY_COUNT_LIMIT = 3
    }
}
