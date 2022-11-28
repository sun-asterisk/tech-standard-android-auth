package com.sun.auth.credentials

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.GsonBuilder
import com.sun.auth.credentials.interceptors.BasicAuthInterceptor
import com.sun.auth.credentials.interceptors.CustomHeadersInterceptor
import com.sun.auth.credentials.interceptors.TokenAuthenticator
import com.sun.auth.credentials.repositories.AuthRepository
import com.sun.auth.credentials.repositories.AuthRepositoryImpl
import com.sun.auth.credentials.repositories.local.AuthLocalDataSource
import com.sun.auth.credentials.repositories.local.SharedPrefApiImpl
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.repositories.remote.AuthRemoteDataSource
import com.sun.auth.credentials.repositories.remote.NonAuthApi
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.credentials.results.AuthException
import com.sun.auth.credentials.utils.ACTION_REFRESH_TOKEN_EXPIRED
import okhttp3.Credentials
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import java.util.concurrent.TimeUnit

class CredentialsAuth private constructor(private val builder: Builder) {
    private lateinit var repository: AuthRepository
    private var client: OkHttpClient? = null
    private var refreshTokenRequest: Request? = null
    private val gson by lazy {
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    }
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(builder.context)
    }

    /**
     * Login user with given info.
     * @param requestBody Request body to send with, `null` if nothing.
     * @param callback Success or failure.
     */
    suspend fun <T : AuthToken> login(requestBody: Any?, callback: AuthCallback<T>) {
        try {
            callback.success(repository.login(builder.loginUrl, requestBody, authTokenClazz()))
        } catch (e: Exception) {
            callback.failure(AuthException(e))
        }
    }

    /**
     * True if the user is logged in, otherwise is false.
     */
    fun <T : AuthToken> isLoggedIn(): Boolean {
        return getToken<T>() != null
    }

    /**
     * Do remove the logged user info.
     * @param doOnLoggedOut Callback after local logged out.
     */
    fun logout(doOnLoggedOut: () -> Unit) {
        removeToken()
        doOnLoggedOut()
    }

    /**
     * Gets the current logged user info.
     */
    fun <T : AuthToken> getToken(): T? {
        return repository.getToken(authTokenClazz())
    }

    /**
     * Sets the refresh token [Request] with full url, request body. Ex
     * ```kt
     *  val json = JsonObject().apply {
     *     addProperty("refresh_token", refreshToken)
     *     addProperty("...", ...)
     *  }
     *  val requestBody = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
     *  val request = Request.Builder()
     *    .url("https://your.url/api/v1/auth_tokens")
     *    .put(requestBody)
     *    .build()
     *  Credentials.getInstance().setRefreshTokenRequest(request)
     * ```
     * @param request the http refresh token request, maybe a [PUT], [POST] or [PATCH] request
     */
    fun setRefreshTokenRequest(request: Request) {
        refreshTokenRequest = request
    }

    /**
     * Manually call refresh token. Try to do it automatically using [TokenAuthenticator].
     * @param request The http request for refreshing token.
     * @param callback The response success or failure when try to call refresh token.
     */
    suspend fun <T : AuthToken> refreshToken(request: Request, callback: AuthCallback<T>?) {
        try {
            refreshTokenRequest = request
            val call = getOrCreateClient().newCall(request)
            callback?.success(repository.refreshToken(call, authTokenClazz()))
        } catch (e: Exception) {
            callback?.failure(AuthException(e))
        }
    }

    internal fun saveToken(token: AuthToken?) {
        token?.let { repository.saveToken(token) }
    }

    internal suspend fun <T : AuthToken> refreshToken(): T? {
        return refreshTokenRequest?.let {
            val call = getOrCreateClient().newCall(it)
            repository.refreshToken(call, authTokenClazz())
        }
    }

    internal fun removeToken() {
        repository.removeToken()
    }

    internal fun notifyTokenExpired() {
        localBroadcastManager.sendBroadcast(Intent(ACTION_REFRESH_TOKEN_EXPIRED))
    }

    private fun build(): CredentialsAuth {
        return this.also { buildDependencies() }
    }

    private fun buildDependencies() {
        val nonAuthApi = buildRetrofit().create(NonAuthApi::class.java)
        val sharedPrefApi = SharedPrefApiImpl(builder.context, gson)
        val remoteDataSource = AuthRemoteDataSource(nonAuthApi)
        val localDataSource = AuthLocalDataSource(sharedPrefApi)

        repository = AuthRepositoryImpl(remoteDataSource, localDataSource, gson)
    }

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL)
            .client(getOrCreateClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private fun getOrCreateClient(): OkHttpClient {
        if (client == null) {
            client = OkHttpClient().newBuilder().apply {
                if (builder.basicAuthentication.isNotBlank()) {
                    addInterceptor(BasicAuthInterceptor(builder.basicAuthentication))
                }
                if (builder.customHeaders != null) {
                    addInterceptor(CustomHeadersInterceptor(builder.customHeaders!!))
                }
            }.addInterceptor(HttpLoggingInterceptor().apply { level = builder.httpLogLevel })
                .connectTimeout(builder.connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(builder.readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(builder.writeTimeout, TimeUnit.MILLISECONDS)
                .build()

        }
        return client!!
    }


    @Suppress("UNCHECKED_CAST")
    private fun <T : AuthToken> authTokenClazz(): Class<T> {
        return builder.authTokenClazz as Class<T>
    }

    companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val DEFAULT_BASE_URL = "https://unused.api.url/"
        private var instance: CredentialsAuth? = null
        private fun createOrGet(builder: Builder) {
            checkNotNull(builder.context) { "Context must be provided!" }
            checkNotNull(builder.loginUrl) { "Please provide full Login URL!" }
            checkNotNull(builder.authTokenClazz) {
                "Token response class must be instanced of AuthToken and provided first!"
            }
            if (instance != null) return
            instance = CredentialsAuth(builder).build()
        }

        fun getInstance(): CredentialsAuth {
            checkNotNull(instance) { "You must init Credentials via Builder first !" }
            return instance!!
        }
    }

    @Suppress("UNUSED")
    class Builder {
        internal lateinit var context: Context
        internal lateinit var loginUrl: String
        internal lateinit var authTokenClazz: Class<*>

        internal var basicAuthentication: String = ""
        internal var httpLogLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY
        internal var connectTimeout = 30000L
        internal var readTimeout = 30000L
        internal var writeTimeout = 30000L
        internal var customHeaders: Headers? = null

        /**
         * Sets context of app. This is required.
         * @param context Your app context.
         */
        fun context(context: Context) = apply { this.context = context.applicationContext }

        /**
         * Sets full login URL. This is required. Ex:
         * ```kt
         *  "https://your.url/api/v1/users/sign_in"
         * ```
         * @param url login url with scheme & path
         */
        fun loginUrl(url: String) = apply { loginUrl = url }

        /**
         * Sets [AuthToken] implementation java class. This help for casting via generic class. This is required.
         * @param clazz the [AuthToken] implementation java class.
         */
        fun <T : AuthToken> authTokenClazz(clazz: Class<T>) = apply { authTokenClazz = clazz }

        /**
         * Sets API connection timeout in milliSeconds. Default is 30s.
         * @param timeout connection timeout.
         */
        fun connectTimeout(timeout: Long) = apply { connectTimeout = timeout }

        /**
         * Sets API read timeout in milliSeconds. Default is 30s.
         * @param timeout read timeout.
         */
        fun readTimeout(timeout: Long) = apply { readTimeout = timeout }

        /**
         * Sets API write timeout in milliSeconds. Default is 30s.
         * @param timeout write timeout.
         */
        fun writeTimeout(timeout: Long) = apply { writeTimeout = timeout }

        /**
         * Sets http debug log level. Default is [HttpLoggingInterceptor.Level.BODY]
         * @param logLevel http debug log level.
         */
        fun httpLogLevel(logLevel: HttpLoggingInterceptor.Level) = apply { httpLogLevel = logLevel }

        /**
         * Sets extras headers for authentication requests.
         * @param headers See more [Headers].
         */
        fun headers(headers: Headers) = apply { customHeaders = headers }

        /**
         * Sets basic authentication for API request via username/password.
         * @param username username of basic authentication.
         * @param password password of basic authentication.
         */
        fun basicAuthentication(username: String?, password: String?) = apply {
            if (!username.isNullOrBlank() && !password.isNullOrBlank()) {
                basicAuthentication = Credentials.basic(username, password)
            }
        }

        fun build() = createOrGet(this)
    }
}
