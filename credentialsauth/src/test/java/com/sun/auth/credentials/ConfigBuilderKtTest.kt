package com.sun.auth.credentials

import android.app.Application
import com.sun.auth.credentials.base.BaseUnitTest
import com.sun.auth.credentials.repositories.model.AuthToken
import io.mockk.*
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
                connectTimeout = 10000L
                readTimeout = 10000L
                writeTimeout = 10000L
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

        assert(config.captured.connectTimeout == 10000L)
        assert(config.captured.writeTimeout == 10000L)
        assert(config.captured.readTimeout == 10000L)
        assert(config.captured.basicAuthentication == "")
    }
}
