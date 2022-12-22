package com.sun.auth.credentials

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
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
import com.sun.auth.credentials.results.AuthTokenChanged
import com.sun.auth.credentials.utils.ACTION_REFRESH_TOKEN_EXPIRED
import com.sun.auth.credentials.utils.PREF_AUTH_TOKEN
import okhttp3.Credentials
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
    private val sharedPrefApi by lazy { SharedPrefApiImpl(builder.context, gson) }
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        LocalBroadcastManager.getInstance(builder.context)
    }

    private val sharedPrefChangeListener =
        OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_AUTH_TOKEN) {
                updateRefreshTokenRequest(builder.authTokenChanged?.onTokenUpdate(getToken()))
            }
        }

    /**
     * SignIn user with given info.
     * @param requestBody Request body to send with, `null` if nothing.
     * @param callback Success or failure.
     */
    suspend fun <T : AuthToken> signIn(requestBody: Any?, callback: AuthCallback<T>) {
        try {
            callback.success(repository.signIn(builder.signInUrl, requestBody, authTokenClazz()))
        } catch (e: Exception) {
            callback.failure(AuthException(e))
        }
    }

    /**
     * True if the user is logged in, otherwise is false.
     */
    fun <T : AuthToken> isSignedIn(): Boolean {
        return getToken<T>() != null
    }

    /**
     * Do remove the signed out user info.
     * @param doOnSignedOut Callback after local signed out.
     */
    fun signOut(doOnSignedOut: () -> Unit) {
        removeToken()
        doOnSignedOut()
    }

    /**
     * Gets the current logged user info.
     */
    fun <T : AuthToken> getToken(): T? {
        return repository.getToken(authTokenClazz())
    }

    /**
     * Manually call refresh token. (Try to do this automatically by using [TokenAuthenticator])
     * @param request The refresh token [Request] with full url, request body. Ex
     * ```kt
     *  val json = JsonObject().apply {
     *     addProperty("refresh_token", refreshToken)
     *     addProperty("...", ...)
     *  }
     *  val requestBody = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
     *  val request = Request.Builder()
     *    .url("https://your.url/api/v1/auth_tokens")
     *    .post(requestBody)
     *    .build()
     * ```
     * @param callback The response success or failure when try to call refresh token.
     */
    suspend fun <T : AuthToken> refreshToken(request: Request, callback: AuthCallback<T>?) {
        try {
            val call = getOrCreateClient().newCall(request)
            callback?.success(repository.refreshToken(call, authTokenClazz()))
        } catch (e: Exception) {
            callback?.failure(AuthException(e))
        }
    }

    internal suspend fun <T : AuthToken> refreshToken(): T? {
        checkNotNull(refreshTokenRequest) {
            "You must build refresh token request first via Builder.authTokenChanged()!"
        }
        return refreshTokenRequest?.let {
            val call = getOrCreateClient().newCall(it)
            repository.refreshToken(call, authTokenClazz())
        }
    }

    internal fun saveToken(token: AuthToken?) {
        token?.let { repository.saveToken(token) }
    }

    internal fun removeToken() {
        unregisterTokenChanged()
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
        val remoteDataSource = AuthRemoteDataSource(nonAuthApi)
        val localDataSource = AuthLocalDataSource(sharedPrefApi)

        registerForTokenChanged()
        repository = AuthRepositoryImpl(remoteDataSource, localDataSource, gson)
    }

    private fun buildRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(DEFAULT_BASE_URL) // This base url will be replaced by full sign in url
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

    private fun updateRefreshTokenRequest(request: Request?) {
        refreshTokenRequest = request
    }

    private fun registerForTokenChanged() {
        sharedPrefApi.registerOnSharedPreferenceChangeListener(sharedPrefChangeListener)
    }

    private fun unregisterTokenChanged() {
        sharedPrefApi.unregisterOnSharedPreferenceChangeListener(sharedPrefChangeListener)
    }

    companion object {
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private const val DEFAULT_BASE_URL = "https://unused.api.url/"
        private var instance: CredentialsAuth? = null
        private fun createOrGet(builder: Builder) {
            checkNotNull(builder.context) { "Context must be provided!" }
            checkNotNull(builder.signInUrl) { "Please provide full signIn URL!" }
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
        internal lateinit var signInUrl: String
        internal lateinit var authTokenClazz: Class<*>

        internal var authTokenChanged: AuthTokenChanged<*>? = null
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
         * Sets full signIn URL. This is required. Ex:
         * ```kt
         *  "https://your.url/api/v1/users/sign_in"
         * ```
         * @param url signIn url with scheme & path
         */
        fun signInUrl(url: String) = apply { signInUrl = url }

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
         * Listens for token update and update refresh token request if needed.
         * @param listener The callback will be run when token is changed.
         */
        fun <T : AuthToken> authTokenChanged(listener: AuthTokenChanged<T>) =
            apply { authTokenChanged = listener }

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

        /**
         * Build the CredentialsAuth instance.
         * @return CredentialsAuth instance.
         */
        fun build() = createOrGet(this)
    }
}
