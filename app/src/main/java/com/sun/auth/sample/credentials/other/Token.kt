package com.sun.auth.sample.credentials.other

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.sun.auth.credentials.repositories.model.AuthToken

data class Token(
    @Expose @SerializedName("id") val id: String? = null,
    @Expose @SerializedName("user_id") val userId: String? = null,
    @Expose @SerializedName("token") val accessToken: String? = null,
    @Expose @SerializedName("refresh_token") val refreshToken: String? = null,
    @Expose @SerializedName("expired_at") val expiresIn: String? = null
) : AuthToken {
    override val crAccessToken: String
        get() = accessToken.orEmpty()
    override var crRefreshToken: String? = null
        get() = refreshToken ?: field
        set(value) {
            if (field == value) return
            field = value
        }
}
