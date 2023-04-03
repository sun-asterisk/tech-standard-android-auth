package com.sun.auth.biometricauth

internal interface SharedPrefApi {
    fun <T> put(key: String, data: T)
    fun <T> get(key: String, type: Class<T>, default: T? = null): T?
    fun removeKey(key: String)
    fun clear()
}
