package com.sun.auth.sample.biometrics.login

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sun.auth.credentials.CredentialsAuth
import com.sun.auth.credentials.results.AuthCallback
import com.sun.auth.sample.R
import com.sun.auth.sample.SingleLiveEvent
import com.sun.auth.sample.model.Token
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel() : ViewModel() {

    private val _signInFormState = MutableLiveData<LoginFormState>()
    val signInFormState: LiveData<LoginFormState> = _signInFormState

    private val _credentialsAuthResult = SingleLiveEvent<LoginResult>()
    val credentialsAuthResult: LiveData<LoginResult> = _credentialsAuthResult

    fun signIn(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            CredentialsAuth.signIn(
                requestBody = LoginRequest(username, password),
                callback = object : AuthCallback<Token> {
                    override fun onResult(data: Token?, error: Throwable?) {
                        _credentialsAuthResult.postValue(LoginResult(token = data, error = error))
                    }
                },
            )
        }
    }

    fun signOut(callback: () -> Unit) {
        CredentialsAuth.signOut(callback)
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _signInFormState.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _signInFormState.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _signInFormState.value = LoginFormState(isDataValid = true)
        }
    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
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
