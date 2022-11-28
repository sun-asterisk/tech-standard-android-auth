package com.sun.auth.credentials.repositories.local

import com.sun.auth.credentials.repositories.model.AuthToken
import com.sun.auth.credentials.utils.PREF_LOGIN_TOKEN

internal class AuthLocalDataSource(private val sharedPrefApi: SharedPrefApi) {
    fun <T : AuthToken> saveToken(response: T) {
        sharedPrefApi.put(PREF_LOGIN_TOKEN, response)
    }

    fun <T : AuthToken> getToken(clazz: Class<T>): T? {
        return sharedPrefApi.get(PREF_LOGIN_TOKEN, clazz)
    }

    fun removeToken() {
        sharedPrefApi.removeKey(PREF_LOGIN_TOKEN)
    }
}
