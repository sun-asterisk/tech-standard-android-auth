package com.sun.auth.credentials.interceptors

import com.sun.auth.credentials.utils.BASIC_AUTHORIZATION
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Basic authenticator using basic credential username/password via headers.Ex:
 * ```
 *  Authorization: [Basic authentication String]
 * ```
 * @param basicAuthentication the basic authentication created by calling:
 * ```kt
 *     Credentials.basic(userName, password)
 * ```
 */
internal class BasicAuthInterceptor(private val basicAuthentication: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        if (basicAuthentication.isNotBlank()) {
            builder.addHeader(BASIC_AUTHORIZATION, basicAuthentication)
        }
        return chain.proceed(builder.build())
    }
}
