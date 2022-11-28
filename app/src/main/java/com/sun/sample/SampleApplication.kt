package com.sun.sample

import android.app.Application
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.sample.credentials.Token
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CredentialsAuth.Builder()
            .context(applicationContext)
            .loginUrl(url = "https://your.url/api/v1/users/sign_in")
            .basicAuthentication(username = "username", password = "password")
            .authTokenClazz(Token::class.java)
            .build()
        if (CredentialsAuth.getInstance().isLoggedIn<Token>()) {
            CredentialsAuth.getInstance().setRefreshTokenRequest(buildRefreshTokenRequest())
        }
    }

    private fun buildRefreshTokenRequest(): Request {
        val json = JsonObject().apply {
            addProperty("refresh_token", getToken()?.crRefreshToken.orEmpty())
        }
        val body = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
        return Request.Builder()
            .url("https://your.url/api/v1/auth_tokens")
            // .put("{refresh_token: ${getToken()?.crRefreshToken.orEmpty()}}".toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE))
            // .post("{refresh_token: ${getToken()?.crRefreshToken.orEmpty()}}".toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE))
            .put(body)
            .patch(
                MultipartBody.Builder().addPart(
                    MultipartBody.Part.createFormData(
                        "refresh_token",
                        getToken()?.crRefreshToken.orEmpty()
                    )
                ).build()
            ).build()
    }
    private fun getToken(): Token? {
       return CredentialsAuth.getInstance().getToken()
    }
}
