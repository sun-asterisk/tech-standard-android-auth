package com.sun.sample.credentials

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @Expose @SerializedName("email") val userName: String? = null,
    @Expose @SerializedName("password") val password: String? = null,
)