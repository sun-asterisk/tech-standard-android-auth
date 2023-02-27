package com.sun.auth.credentials.repositories.remote

import com.sun.auth.credentials.repositories.remote.api.NonAuthApi
import com.sun.auth.credentials.utils.call
import okhttp3.Call

internal class AuthRemoteDataSource(private val api: NonAuthApi) {
    fun signIn(url: String, requestBody: Any?): String {
        return api.signIn(url, requestBody).call()
    }

    fun refreshToken(request: Call): String {
        return request.call()
    }
}
