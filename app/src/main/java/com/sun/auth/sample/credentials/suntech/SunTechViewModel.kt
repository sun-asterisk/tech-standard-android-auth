package com.sun.auth.sample.credentials.suntech

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.credentials.results.AuthException
import com.sun.auth.sample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class SunTechViewModel : ViewModel() {

    private val _signInFormState = MutableLiveData<SignInFormState>()
    val signInFormState: LiveData<SignInFormState> = _signInFormState

    private val _credentialAuthResult = MutableLiveData<SunTechResult>()
    val credentialAuthResult: LiveData<SunTechResult> = _credentialAuthResult

    private val _refreshTokenResult = MutableLiveData<SunTechResult?>()
    val refreshTokenResult: LiveData<SunTechResult?> = _refreshTokenResult

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.getInstance().signIn(
                requestBody = SignInRequest(username, password),
                callback = object : AuthCallback<SunToken> {
                    override fun success(data: SunToken?) {
                        _credentialAuthResult.postValue(SunTechResult(success = data))
                    }

                    override fun failure(exception: AuthException?) {
                        _credentialAuthResult.postValue(SunTechResult(error = exception))
                    }
                }
            )
        }
    }

    fun getToken(): SunToken? {
        return CredentialsAuth.getInstance().getToken()
    }

    fun logout(callback: () -> Unit) {
        CredentialsAuth.getInstance().signOut(callback)
    }

    fun isLoggedIn(): Boolean {
        return CredentialsAuth.getInstance().isSignedIn<SunToken>()
    }

    // Sample for manually refresh token.
    fun refreshToken() {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.getInstance().refreshToken(
                request = buildRefreshTokenRequest(),
                callback = object : AuthCallback<SunToken> {
                    override fun success(data: SunToken?) {
                        _refreshTokenResult.postValue(SunTechResult(success = data))
                    }

                    override fun failure(exception: AuthException?) {
                        _refreshTokenResult.postValue(SunTechResult(error = exception))
                    }
                })
        }
    }

    fun signInDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _signInFormState.value = SignInFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _signInFormState.value = SignInFormState(passwordError = R.string.invalid_password)
        } else {
            _signInFormState.value = SignInFormState(isDataValid = true)
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

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank() && username.length > 5
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}