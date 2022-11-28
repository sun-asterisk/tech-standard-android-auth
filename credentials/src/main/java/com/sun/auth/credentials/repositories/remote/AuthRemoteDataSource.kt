package com.sun.auth.credentials.repositories.remote

import com.sun.auth.credentials.utils.call
import okhttp3.Call

internal class AuthRemoteDataSource(private val api: NonAuthApi) {
    fun login(url: String, requestBody: Any?): String {
        return api.login(url, requestBody).call()
    }

    fun refreshToken(request: Call): String {
        return request.call()
    }
}
