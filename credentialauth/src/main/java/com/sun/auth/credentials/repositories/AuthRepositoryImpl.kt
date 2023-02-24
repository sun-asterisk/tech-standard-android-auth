package com.sun.auth.credentials.repositories

import com.google.gson.Gson
import com.sun.auth.credentials.repositories.local.AuthLocalDataSource
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.repositories.remote.AuthRemoteDataSource
import okhttp3.Call

internal class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val local: AuthLocalDataSource,
    private val gson: Gson,
) : AuthRepository {

    override suspend fun <T : AuthToken> signIn(
        url: String,
        requestBody: Any?,
        responseClazz: Class<T>,
    ): T {
        val json = remote.signIn(url, requestBody)
        val response = gson.fromJson(json, responseClazz)
        local.saveToken(response)
        return response
    }

    override suspend fun <T : AuthToken> refreshToken(
        request: Call,
        responseClazz: Class<T>,
    ): T? {
        val json = remote.refreshToken(request)
        val newToken = gson.fromJson(json, responseClazz)
        local.saveToken(newToken)
        return newToken
    }

    override fun <T : AuthToken> saveToken(token: T) {
        local.saveToken(token)
    }

    override fun <T : AuthToken> getToken(clazz: Class<T>): T? {
        return local.getToken(clazz)
    }

    override fun removeToken() {
        local.removeToken()
    }
}
