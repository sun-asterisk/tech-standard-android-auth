package com.sun.auth.sample.credentials

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sun.auth.credentials.repositories.model.AuthToken

data class Token(
    @Expose
    @SerializedName("token_type")
    val tokenType: String? = null,
    @Expose
    @SerializedName("access_token")
    val accessToken: String? = null,
    @Expose
    @SerializedName("refresh_token")
    val refreshToken: String? = null,
    @Expose
    @SerializedName("expires_at")
    val expiresIn: String? = null,
) : AuthToken {
    override val crAccessToken: String
        get() = accessToken.orEmpty()
    override var crRefreshToken: String? = null
        get() = refreshToken ?: field
}
