package com.sun.auth.credentials.repositories

import com.sun.auth.credentials.repositories.model.AuthToken
import okhttp3.Call

internal interface AuthRepository {
    suspend fun <T : AuthToken> signIn(url: String, requestBody: Any?, responseClazz: Class<T>): T?
    suspend fun <T : AuthToken> refreshToken(
        request: Call,
        responseClazz: Class<T>
    ): T?

    fun <T : AuthToken> saveToken(token: T)
    fun <T : AuthToken> getToken(clazz: Class<T>): T?
    fun removeToken()
}
