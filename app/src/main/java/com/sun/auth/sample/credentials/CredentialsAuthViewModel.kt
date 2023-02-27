package com.sun.auth.sample.credentials

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.sample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class CredentialsAuthViewModel : ViewModel() {

    private val _signInFormState = MutableLiveData<SignInFormState>()
    val signInFormState: LiveData<SignInFormState> = _signInFormState

    private val _credentialsAuthResult = MutableLiveData<AuthResult>()
    val credentialsAuthResult: LiveData<AuthResult> = _credentialsAuthResult

    private val _refreshTokenResult = MutableLiveData<AuthResult?>()
    val refreshTokenResult: LiveData<AuthResult?> = _refreshTokenResult

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.signIn(
                requestBody = SignInRequest(username, password),
                callback = object : AuthCallback<Token> {
                    override fun onResult(data: Token?, error: Throwable?) {
                        _credentialsAuthResult.postValue(
                            AuthResult(success = data, error = error),
                        )
                    }
                },
            )
        }
    }

    fun getToken(): Token? {
        return CredentialsAuth.getToken()
    }

    fun signOut(callback: () -> Unit) {
        CredentialsAuth.signOut(callback)
    }

    fun isSignedIn(): Boolean {
        return CredentialsAuth.isSignedIn<Token>()
    }

    // Sample for manually refresh token.
    fun refreshToken() {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.refreshToken(
                request = buildRefreshTokenRequest(),
                callback = object : AuthCallback<Token> {
                    override fun onResult(data: Token?, error: Throwable?) {
                        _refreshTokenResult.postValue(
                            AuthResult(success = data, error = error),
                        )
                    }
                },
            )
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
