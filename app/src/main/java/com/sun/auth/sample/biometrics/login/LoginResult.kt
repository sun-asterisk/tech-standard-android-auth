package com.sun.auth.sample.biometrics.login

import com.sun.auth.sample.model.Token

data class LoginResult(
    val token: Token? = null,
    val error: Throwable? = null,
)
