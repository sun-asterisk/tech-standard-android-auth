package com.sun.auth.sample.biometrics.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.sample.SingleLiveEvent
import com.sun.auth.sample.biometrics.login.LoginResult
import com.sun.auth.sample.model.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class HomeViewModel : ViewModel() {
    private val _refreshTokenResult = SingleLiveEvent<LoginResult?>()
    val refreshTokenResult: LiveData<LoginResult?> = _refreshTokenResult

    fun getToken(): Token? {
        return CredentialsAuth.getToken()
    }

    fun signOut(callback: () -> Unit) {
        CredentialsAuth.signOut(callback)
    }

    fun refreshToken() {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.refreshToken(
                request = buildRefreshTokenRequest(),
                callback = object : AuthCallback<Token> {
                    override fun onResult(data: Token?, error: Throwable?) {
                        _refreshTokenResult.postValue(LoginResult(token = data, error = error))
                    }
                },
            )
        }
    }

    private fun buildRefreshTokenRequest(): Request {
        val json = JsonObject().apply {
            addProperty("refresh_token", getToken()?.crRefreshToken.orEmpty())
        }
        val body = json.toString().toRequestBody(CredentialsAuth.JSON_MEDIA_TYPE)
        return Request.Builder()
            .url("http://10.0.5.78:8001/api/refresh")
            .post(body)
            .build()
    }
}
