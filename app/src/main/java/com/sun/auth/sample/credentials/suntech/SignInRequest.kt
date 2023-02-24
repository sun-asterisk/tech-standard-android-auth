package com.sun.auth.sample.credentials.suntech

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SignInRequest(
    @Expose
    @SerializedName("username")
    val userName: String? = null,
    @Expose
    @SerializedName("password")
    val password: String? = null,
)
