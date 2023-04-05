package com.sun.auth.sample.biometric

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.sample.R
import com.sun.auth.sample.model.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Suppress("MagicNumber")
class LoginViewModel : ViewModel() {

    private val _signInFormState = MutableLiveData<LoginFormState>()
    val signInFormState: LiveData<LoginFormState> = _signInFormState

    private val _credentialsAuthResult = MutableLiveData<LoginResult>()
    val credentialsAuthResult: LiveData<LoginResult> = _credentialsAuthResult

    private val _refreshTokenResult = MutableLiveData<LoginResult?>()
    val refreshTokenResult: LiveData<LoginResult?> = _refreshTokenResult

    private val _currentToken = MutableLiveData<Token?>()
    val currentToken: LiveData<Token?> = _currentToken

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.signIn(
                requestBody = LoginRequest(username, password),
                callback = object : AuthCallback<Token> {
                    override fun onResult(data: Token?, error: Throwable?) {
                        updateCurrentToken(data)
                        _credentialsAuthResult.postValue(LoginResult(token = data, error = error))
                    }
                },
            )
        }
    }

    fun updateCurrentToken(data: Token?) {
        _currentToken.postValue(data)
    }

    fun getToken(): Token? {
        return _currentToken.value ?: CredentialsAuth.getToken()
    }

    fun getSavedToken(): Token? {
        return CredentialsAuth.getToken()
    }

    fun signOut(callback: () -> Unit) {
        _currentToken.value = null
        CredentialsAuth.signOut(callback)
    }

    fun isSignedIn(): Boolean {
        return _currentToken.value != null || CredentialsAuth.isSignedIn<Token>()
    }

    // Sample for manually refresh token.
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

    fun signInDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _signInFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _signInFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _signInFormState.value = LoginFormState(isDataValid = true)
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
