package com.sun.auth.sample.biometric

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @Expose
    @SerializedName("username")
    val userName: String? = null,
    @Expose
    @SerializedName("password")
    val password: String? = null,
)
