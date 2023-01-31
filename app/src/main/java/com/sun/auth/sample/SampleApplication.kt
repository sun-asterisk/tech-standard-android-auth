package com.sun.auth.sample

import android.app.Application
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthTokenChanged
import com.sun.auth.sample.credentials.other.Token
import com.sun.auth.sample.credentials.suntech.SunToken
import com.sun.auth.social.initSocialAuth
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

//        setupCredentialAuth()
        setupSocialAuth()
        setupSunTechAuth()
    }

    private fun setupCredentialAuth() {
        CredentialsAuth.Builder()
            .context(applicationContext)
            .signInUrl(url = "https://your.url/api/v1/users/sign_in")
            .basicAuthentication(username = "username", password = "password")
            .authTokenClazz(Token::class.java)
            .authTokenChanged(object : AuthTokenChanged<Token> {
                // do this when you want to refresh token automatically
                override fun onTokenUpdate(token: Token?): Request {
                    return buildRefreshTokenRequest(token)
                }
            })
            .build()
    }

    private fun buildRefreshTokenRequest(token: Token?): Request {
        val json = JsonObject().apply {
            addProperty("refresh_token", token?.crRefreshToken.orEmpty())
        }
        val body = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
        return Request.Builder()
            .url("https://your.url/api/v1/auth_tokens")
            //  .put("{refresh_token: ${token?.crRefreshToken.orEmpty()}}".toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE))
            //  .post("{refresh_token: ${token?.crRefreshToken.orEmpty()}}".toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE))
            //  .put(body)
            .patch(
                MultipartBody.Builder().addPart(
                    MultipartBody.Part.createFormData(
                        "refresh_token",
                        token?.crRefreshToken.orEmpty()
                    )
                ).build()
            )
            .build()
    }

    private fun setupSocialAuth() {
        initSocialAuth {
            google(getString(R.string.google_web_client_id)) {
                enableOneTapSignIn = true
                enableFilterByAuthorizedAccounts = true
            }
            facebook(
                appId = getString(R.string.facebook_app_id),
                clientToken = getString(R.string.facebook_client_token)
            ) {
                readPermissions = listOf("email", "public_profile")
                enableAppEvent = false
                useFacebookLoginButton = true
            }
        }
        /*
        Or use each social config.
        initGoogleAuth(getString(R.string.google_web_client_id)) {
            enableOneTapSignIn = true
        }
        */
    }

    private fun setupSunTechAuth() {
        CredentialsAuth.Builder()
            .context(applicationContext)
            .signInUrl(url = "http://10.0.5.78:8001/api/login")
            .authTokenClazz(SunToken::class.java)
            .authTokenChanged(object : AuthTokenChanged<SunToken> {
                // do this when you want to refresh token automatically
                override fun onTokenUpdate(token: SunToken?): Request {
                    return buildSunTechRefreshTokenRequest(token)
                }
            })
            .build()
    }

    private fun buildSunTechRefreshTokenRequest(token: SunToken?): Request {
        val json = JsonObject().apply {
            addProperty("refresh_token", token?.crRefreshToken.orEmpty())
        }
        val body = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
        return Request.Builder()
            .url("http://10.0.5.78:8001/api/refresh")
            .post(body)
            .build()
    }
}
