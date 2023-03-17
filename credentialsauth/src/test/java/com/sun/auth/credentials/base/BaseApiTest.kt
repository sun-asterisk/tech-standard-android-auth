package com.sun.auth.credentials.base

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before

open class BaseApiTest {
    protected val mockServer: MockWebServer by lazy { MockWebServer() }
    protected val clientBuilder: OkHttpClient.Builder by lazy { OkHttpClient().newBuilder() }

    @Before
    fun setup() {
        mockServer.start()
    }

    @After
    fun tearDown() {
        mockServer.shutdown()
    }
}
