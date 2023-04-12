package com.sun.auth.google.standard

import android.app.Application
import android.text.TextUtils
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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
        mockkObject(GoogleStandardAuth)
        mockkStatic(TextUtils::class)
        every { TextUtils.isEmpty(any()) } answers {
            arg<String?>(0).isNullOrEmpty()
        }
    }

    @Test
    fun initGoogleAuth() {
        val clientId = "test_client_id"
        val options = GoogleSignInOptions.Builder().build()
        every {
            GoogleConfig.apply(any(), any(), any())
        } answers { callOriginal() }

        application.initGoogleAuth(
            webClientId = clientId,
            signInOptions = options,
            setup = {
                enableOneTapSignIn = false
                enableFilterByAuthorizedAccounts = false
            },
        )

        verify {
            GoogleConfig.apply(
                clientId = clientId,
                options = options,
                setup = any(),
            )
        }
        GoogleStandardAuth.getPrivateProperty<GoogleStandardAuth, GoogleConfig>("config")
            ?.also { config ->
                assert(config.webClientId == clientId)
                assert(!config.enableOneTapSignIn)
                assert(!config.enableFilterByAuthorizedAccounts)
            }
    }
}
