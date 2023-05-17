package com.sun.auth.google.firebase

import android.app.Application
import android.text.TextUtils
import com.sun.auth.core.getPrivateProperty
import io.mockk.*
import org.junit.Before
import org.junit.Test

class ConfigBuilderKtTest {
    private lateinit var application: Application

    @Before
    fun setup() {
        application = mockk(relaxed = true)
        mockkObject(GoogleConfig.Companion)
        mockkObject(GoogleFirebaseAuth)
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } answers {
            arg<String?>(0).isNullOrEmpty()
        }
    }

    @Test
    fun initGoogleFirebaseAuth() {
        val clientId = "test_client_id"
        every {
            GoogleConfig.apply(any(), any())
        } answers { callOriginal() }

        application.initGoogleAuth(
            webClientId = clientId,
            setup = {
                enableOneTapSignIn = false
                enableFilterByAuthorizedAccounts = false
            },
        )

        verify {
            GoogleConfig.apply(clientId = clientId, setup = any())
        }
        GoogleFirebaseAuth.getPrivateProperty<GoogleFirebaseAuth, GoogleConfig>("config")
            ?.also { config ->
                assert(config.webClientId == clientId)
                assert(!config.enableOneTapSignIn)
                assert(!config.enableFilterByAuthorizedAccounts)
                assert(!config.enableLinkAccounts)
            }
    }
}
