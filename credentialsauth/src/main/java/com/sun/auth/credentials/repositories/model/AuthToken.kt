package com.sun.auth.credentials.repositories.model

/**
 * Authentication token interface, your token must be implement it. Ex:
 * ```kt
 *  data class Token(
 *      @Expose @SerializedName("token") val accessToken: String? = null,
 *      @Expose @SerializedName("refresh_token") val refreshToken: String? = null,
 *      @Expose @SerializedName("expired_at") val expiresIn: String? = null
 *  ) : AuthToken {
 *      override val crAccessToken: String
 *          get() = accessToken.orEmpty()
 *      override var crRefreshToken: String? = null
 *          get() = refreshToken ?: field
 *  }
 * ```
 */
interface AuthToken {
    val crAccessToken: String
    var crRefreshToken: String?
}
