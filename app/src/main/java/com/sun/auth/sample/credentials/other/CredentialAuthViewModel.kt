package com.sun.auth.sample.credentials.other

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.credentials.results.AuthException
import com.sun.auth.sample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.Request

class CredentialAuthViewModel : ViewModel() {

    private val _signInFormState = MutableLiveData<SignInFormState>()
    val signInFormState: LiveData<SignInFormState> = _signInFormState

    private val _credentialAuthResult = MutableLiveData<CredentialAuthResult>()
    val credentialAuthResult: LiveData<CredentialAuthResult> = _credentialAuthResult

    private val _refreshTokenResult = MutableLiveData<CredentialAuthResult?>()
    val refreshTokenResult: LiveData<CredentialAuthResult?> = _refreshTokenResult

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.getInstance().signIn(
                requestBody = SignInRequest(username, password),
                callback = object : AuthCallback<Token> {
                    override fun success(data: Token?) {
                        _credentialAuthResult.postValue(CredentialAuthResult(success = data))
                    }

                    override fun failure(exception: AuthException?) {
                        _credentialAuthResult.postValue(CredentialAuthResult(error = exception))
                    }
                },
            )
        }
    }

    fun getToken(): Token? {
        return CredentialsAuth.getInstance().getToken()
    }

    fun signOut(callback: () -> Unit) {
        CredentialsAuth.getInstance().signOut(callback)
    }

    fun isSignedIn(): Boolean {
        return CredentialsAuth.getInstance().isSignedIn<Token>()
    }

    // Sample for manually refresh token.
    fun refreshToken() {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.getInstance().refreshToken(
                request = buildRefreshTokenRequest(),
                callback = object : AuthCallback<Token> {
                    override fun success(data: Token?) {
                        _refreshTokenResult.postValue(CredentialAuthResult(success = data))
                    }

                    override fun failure(exception: AuthException?) {
                        _refreshTokenResult.postValue(CredentialAuthResult(error = exception))
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
        return Request.Builder()
            .url("https://your.url/api/v1/auth_tokens")
            .patch(
                MultipartBody.Builder().addPart(
                    MultipartBody.Part.createFormData(
                        "refresh_token",
                        getToken()?.crRefreshToken.orEmpty(),
                    ),
                ).build(),
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
