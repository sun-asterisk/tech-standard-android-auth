package com.sun.auth.sample.credentials.other

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class SignInRequest(
    @Expose
    @SerializedName("email")
    val userName: String? = null,
    @Expose
    @SerializedName("password")
    val password: String? = null,
)
