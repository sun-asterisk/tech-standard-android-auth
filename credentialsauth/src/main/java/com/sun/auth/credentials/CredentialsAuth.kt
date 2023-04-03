package com.sun.auth.credentials

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.GsonBuilder
import com.sun.auth.core.weak
import com.sun.auth.credentials.interceptors.BasicAuthInterceptor
import com.sun.auth.credentials.interceptors.CustomHeadersInterceptor
import com.sun.auth.credentials.interceptors.TokenAuthenticator
import com.sun.auth.credentials.repositories.AuthRepository
import com.sun.auth.credentials.repositories.AuthRepositoryImpl
import com.sun.auth.credentials.repositories.local.AuthLocalDataSource
import com.sun.auth.credentials.repositories.local.api.SharedPrefApiImpl
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.repositories.remote.AuthRemoteDataSource
import com.sun.auth.credentials.repositories.remote.api.NonAuthApi
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.credentials.utils.ACTION_REFRESH_TOKEN_EXPIRED
import com.sun.auth.credentials.utils.PREF_AUTH_TOKEN
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Suppress("TooManyFunctions")
object CredentialsAuth {
    val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    private const val DEFAULT_BASE_URL = "https://unused.api.url/"

    private lateinit var repository: AuthRepository
    private var context: Context? by weak(null)
    private lateinit var config: CredentialsAuthConfig
    private var client: OkHttpClient? = null
    private var refreshTokenRequest: Request? = null
    private val gson by lazy {
        GsonBuilder()
            .enableComplexMapKeySerialization()
            .excludeFieldsWithoutExposeAnnotation()
            .create()
    }
    private val sharedPrefApi by lazy {
        checkNotNull(context) {
            "Context must be provided!"
        }
        SharedPrefApiImpl(context!!, gson)
    }
    private val localBroadcastManager: LocalBroadcastManager by lazy {
        checkNotNull(context) {
            "Context must be provided!"
        }
        LocalBroadcastManager.getInstance(context!!)
    }

    private val sharedPrefChangeListener =
        OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_AUTH_TOKEN) {
                updateRefreshTokenRequest(config.authTokenChanged?.onTokenUpdate(getToken()))
            }
        }

    internal fun initialize(context: Context, config: CredentialsAuthConfig) {
        this.context = context.applicationContext
        this.config = config
        build()
    }

    /**
     * SignIn user with given info.
     * @param requestBody Request body to send with, `null` if nothing.
     * @param callback Success or failure callback.
     */
    suspend fun <T : AuthToken> signIn(requestBody: Any?, callback: AuthCallback<T>) {
        try {
            callback.onResult(
                data = repository.signIn(
                    config.signInUrl,
                    requestBody,
                    authTokenClazz(),
                ),
            )
        } catch (e: Exception) {
            callback.onResult(error = e)
        }
    }

    /**
     * True if the user is signed in, otherwise is false.
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
            callback?.onResult(data = repository.refreshToken(call, authTokenClazz()))
        } catch (e: Exception) {
            callback?.onResult(error = e)
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
                if (config.basicAuthentication.isNotBlank()) {
                    addInterceptor(BasicAuthInterceptor(config.basicAuthentication))
                }
                if (config.customHeaders != null) {
                    addInterceptor(CustomHeadersInterceptor(config.customHeaders!!))
                }
            }.addInterceptor(HttpLoggingInterceptor().apply { level = config.httpLogLevel })
                .connectTimeout(config.connectTimeout, TimeUnit.MILLISECONDS)
                .readTimeout(config.readTimeout, TimeUnit.MILLISECONDS)
                .writeTimeout(config.writeTimeout, TimeUnit.MILLISECONDS)
                .build()
        }
        return client!!
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : AuthToken> authTokenClazz(): Class<T> {
        return config.authTokenClazz as Class<T>
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
}
