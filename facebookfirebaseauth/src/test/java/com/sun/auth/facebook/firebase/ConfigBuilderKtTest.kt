package com.sun.auth.facebook.firebase

import android.app.Application
import com.facebook.FacebookSdk
import com.sun.auth.core.getPrivateProperty
import io.mockk.*
import org.junit.Before
import org.junit.Test

class ConfigBuilderKtTest {
    private lateinit var application: Application

    @Before
    fun setup() {
        application = mockk(relaxed = true)
        mockkObject(FacebookConfig.Companion)
        mockkObject(FacebookFirebaseAuth)
        mockkStatic(FacebookSdk::class)
    }

    @Test
    fun initFacebookFirebaseAuth() {
        every { FacebookSdk.sdkInitialize(any()) } returns mockk()
        every {
            FacebookConfig.apply(any(), any(), any(), any())
        } answers { callOriginal() }

        application.initFacebookFirebaseAuth(
            appId = "1",
            clientToken = "1",
            setup = {
                readPermissions = listOf("email", "public_profile")
                enableLoginStatus = false
                enableAppEvent = false
                useFacebookLoginButton = true
            },
        )

        verify {
            FacebookConfig.apply(
                context = application,
                appId = "1",
                clientToken = "1",
                setup = any(),
            )
        }

        FacebookFirebaseAuth.getPrivateProperty<FacebookFirebaseAuth, FacebookConfig>("config")
            ?.also { config ->
                assert(config.readPermissions.size == 2)
                assert(!config.enableAppEvent)
                assert(config.useFacebookLoginButton)
                assert(!config.enableLoginStatus)
            }
    }
}
