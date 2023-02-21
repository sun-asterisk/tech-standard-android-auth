package com.sun.auth.credentials.interceptors

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Add custom header to your request.
 * ```kt
 *    Accept-Language: en-US,en;q=0.5
 *    Accept-Encoding: gzip, deflate, br
 *    Custom-header: Custom-header-value
 * ```
 * @param headers See more at [Headers]
 */
internal class CustomHeadersInterceptor(private val headers: Headers) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()
        for ((key, value) in headers) {
            builder.addHeader(key, value)
        }
        return chain.proceed(builder.build())
    }
}
