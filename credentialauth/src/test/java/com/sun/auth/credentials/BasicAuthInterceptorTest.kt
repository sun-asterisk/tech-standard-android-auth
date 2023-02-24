package com.sun.auth.credentials

import com.sun.auth.base.BaseApiTest
import com.sun.auth.credentials.interceptors.BasicAuthInterceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.* // ktlint-disable no-wildcard-imports
import org.junit.Test

class BasicAuthInterceptorTest : BaseApiTest() {
    private val basicAuthen = "BasicAuthen"
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
    private lateinit var interceptor: BasicAuthInterceptor

    @Test
    fun intercepted() {
        interceptor = BasicAuthInterceptor(basicAuthen)
        val client = clientBuilder.addInterceptor(interceptor)
            .addInterceptor { chain ->
                val addedHeader = chain.request().headers["Authorization"]
                assertNotNull(addedHeader)
                assertEquals(addedHeader, basicAuthen)
                mockResponse
            }.build()
        val response = client.newCall(request).execute()
        assertEquals(response, mockResponse)
    }

    @Test
    fun noIntercepted() {
        val client = clientBuilder.addInterceptor { chain ->
            assertNull(chain.request().headers["Authorization"])
            mockResponse
        }.build()

        val response = client.newCall(request).execute()
        assertEquals(response, mockResponse)
    }
}
