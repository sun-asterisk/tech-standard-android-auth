package com.sun.auth.credentials

import com.sun.auth.base.BaseApiTest
import com.sun.auth.credentials.interceptors.CustomHeadersInterceptor
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test

class CustomHeadersInterceptorTest : BaseApiTest() {
    private lateinit var interceptor: CustomHeadersInterceptor
    private val emptyResponse = "{}"
    private val request by lazy { Request.Builder().url(mockServer.url("/")).build() }
    private val mockResponse by lazy {
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("intercepted!")
            .body(emptyResponse.toResponseBody(("text/plain; charset=utf-8").toMediaType()))
            .build()
    }

    @Test
    fun intercepted() {
        val headers = Headers.Builder()
            .add("header_1", "header_1_value")
            .build()
        interceptor = CustomHeadersInterceptor(headers)
        val client = clientBuilder.addInterceptor(interceptor)
            .addInterceptor { chain ->
                val addedHeader = chain.request().headers["header_1"]
                assertNotNull(addedHeader)
                assertEquals(addedHeader, "header_1_value")
                mockResponse
            }.build()

        val response = client.newCall(request).execute()
        assertEquals(response, mockResponse)
    }

    @Test
    fun noIntercepted() {
        val client = clientBuilder
            .addInterceptor { chain ->
                assertNull(chain.request().headers["header_1"])
                mockResponse
            }.build()

        val response = client.newCall(request).execute()
        assertEquals(response, mockResponse)
    }
}
