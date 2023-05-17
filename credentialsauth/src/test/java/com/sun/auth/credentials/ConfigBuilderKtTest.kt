package com.sun.auth.credentials

import android.app.Application
import com.sun.auth.credentials.base.BaseUnitTest
import com.sun.auth.credentials.repositories.model.AuthToken
import io.mockk.*
import okhttp3.Headers
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test

class ConfigBuilderKtTest : BaseUnitTest() {
    private lateinit var application: Application
    override fun setup() {
        super.setup()
        application = mockk()
        mockkObject(CredentialsAuthConfig.Companion)
        mockkObject(CredentialsAuth)
    }

    @Test
    fun initCredentialsAuth() {
        val config = slot<CredentialsAuthConfig>()

        every {
            CredentialsAuthConfig.apply(any(), any(), any())
        } answers { callOriginal() }

        every { CredentialsAuth.initialize(any(), capture(config)) } returns mockk()

        application.initCredentialsAuth(
            signInUrl = "url",
            authTokenClazz = AuthToken::class.java,
            setup = {
                readTimeout = 10000L
                writeTimeout = 10000
                connectTimeout = 10000L
                httpLogLevel = HttpLoggingInterceptor.Level.HEADERS
                customHeaders = Headers.headersOf()
                basicAuthentication = ""
            },
        )

        verify {
            CredentialsAuthConfig.apply(
                signInUrl = "url",
                authTokenClazz = AuthToken::class.java,
                setup = any(),
            )
        }
        verify { CredentialsAuth.initialize(any(), config.captured) }
        with(config.captured) {
            assert(signInUrl == "url")
            assert(connectTimeout == 10000L)
            assert(writeTimeout == 10000L)
            assert(readTimeout == 10000L)
            assert(httpLogLevel == HttpLoggingInterceptor.Level.HEADERS)
            assert(authTokenClazz == AuthToken::class.java)
            assert(basicAuthentication == "")
        }
    }
}
