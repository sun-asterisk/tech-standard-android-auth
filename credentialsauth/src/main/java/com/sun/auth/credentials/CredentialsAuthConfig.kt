package com.sun.auth.credentials

import com.sun.auth.core.ConfigFunction
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.results.AuthTokenChanged
import okhttp3.Headers
import okhttp3.logging.HttpLoggingInterceptor

class CredentialsAuthConfig {
    /**
     * Sets full signIn URL with scheme & path. This is required. Ex:
     * ```kt
     *  "https://your.url/api/v1/users/sign_in"
     * ```
     */
    internal lateinit var signInUrl: String

    /**
     * Sets [AuthToken] implementation java class. This helps for casting via generic class. This is required.
     */
    internal lateinit var authTokenClazz: Class<*>

    /**
     * Listens for token update and update refresh token request if needed.
     */
    var authTokenChanged: AuthTokenChanged<*>? = null

    /**
     * Sets basic authentication for API request via username/password.
     *
     * Ex: `okhttp3.Credentials.basic(username, password, Charsets.UTF_8)`
     */
    var basicAuthentication: String = ""

    /**
     * Sets http debug log level. Default is [HttpLoggingInterceptor.Level.BODY]
     */
    var httpLogLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY

    /**
     * Sets API connection timeout in milliSeconds. Default is 30s.
     */
    var connectTimeout = 30000L

    /**
     * Sets API read timeout in milliSeconds. Default is 30s.
     */
    var readTimeout = 30000L

    /**
     * Sets API write timeout in milliSeconds. Default is 30s.
     */
    var writeTimeout = 30000L

    /**
     * Sets extras headers for authentication requests.
     */
    var customHeaders: Headers? = null

    companion object {
        internal fun apply(
            signInUrl: String,
            authTokenClazz: Class<*>,
            setup: ConfigFunction<CredentialsAuthConfig>? = null,
        ): CredentialsAuthConfig {
            val config = CredentialsAuthConfig().apply {
                this.signInUrl = signInUrl
                this.authTokenClazz = authTokenClazz
            }
            setup?.invoke(config)
            return config
        }
    }
}
