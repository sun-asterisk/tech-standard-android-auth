package com.sun.auth.credentials.repositories.local

import com.sun.auth.credentials.repositories.local.api.SharedPrefApi
import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.utils.PREF_AUTH_TOKEN

internal class AuthLocalDataSource(private val sharedPrefApi: SharedPrefApi) {
    fun <T : AuthToken> saveToken(response: T) {
        sharedPrefApi.put(PREF_AUTH_TOKEN, response)
    }

    fun <T : AuthToken> getToken(clazz: Class<T>): T? {
        return sharedPrefApi.get(PREF_AUTH_TOKEN, clazz)
    }

    fun removeToken() {
        sharedPrefApi.removeKey(PREF_AUTH_TOKEN)
    }
}
