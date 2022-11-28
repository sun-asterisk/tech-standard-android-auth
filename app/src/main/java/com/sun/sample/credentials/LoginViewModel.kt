package com.sun.sample.credentials

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.credentials.results.AuthException
import com.sun.sample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.Request

class LoginViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _authenResult = MutableLiveData<AuthenResult>()
    val authenResult: LiveData<AuthenResult> = _authenResult

    private val _refreshTokenResult = MutableLiveData<AuthenResult?>()
    val refreshTokenResult: LiveData<AuthenResult?> = _refreshTokenResult

    fun login(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.getInstance().login(
                requestBody = LoginRequest(username, password),
                callback = object : AuthCallback<Token> {
                    override fun success(data: Token?) {
                        _authenResult.postValue(AuthenResult(success = data))
                    }

                    override fun failure(exception: AuthException?) {
                        _authenResult.postValue(AuthenResult(error = exception))
                    }
                }
            )
        }
    }

    fun getToken(): Token? {
        return CredentialsAuth.getInstance().getToken()
    }

    fun logout(callback: () -> Unit) {
        CredentialsAuth.getInstance().logout(callback)
    }

    fun isLoggedIn(): Boolean {
        return CredentialsAuth.getInstance().isLoggedIn<Token>()
    }

    // Sample for manually refresh token.
    fun refreshToken() {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.getInstance().refreshToken(
                request = buildRefreshTokenRequest(),
                callback = object : AuthCallback<Token> {
                    override fun success(data: Token?) {
                        _refreshTokenResult.postValue(AuthenResult(success = data))
                    }

                    override fun failure(exception: AuthException?) {
                        _refreshTokenResult.postValue(AuthenResult(error = exception))
                    }
                })
        }
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    private fun buildRefreshTokenRequest(): Request {
        return Request.Builder()
            .url("https://your.url/api/v1/auth_tokens")
            .patch(
                MultipartBody.Builder().addPart(
                    MultipartBody.Part.createFormData(
                        "refresh_token",
                        getToken()?.crRefreshToken.orEmpty()
                    )
                ).build()
            ).build()
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains('@')) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}